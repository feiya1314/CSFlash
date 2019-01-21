package com.yufeiblog.cassandra.loadbalance;

import com.datastax.driver.core.*;
import com.google.common.collect.AbstractIterator;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DCSwitchTokenAwarePolicy implements SwitchLoadbalancePolicy {

    private SwitchLoadbalancePolicy childPolicy;
    private String localDC;
    private boolean isShard;
    private boolean isDegraded;
    private Cluster cluster;
    private DCSwitchLoadbalance loadbalance;

    public DCSwitchTokenAwarePolicy(DCSwitchLoadbalance loadbalance) {
        this.loadbalance = loadbalance;
    }

    @Override
    public void setLoaclDC(String dc) {
        this.localDC = dc;
        childPolicy.setLoaclDC(dc);
    }

    @Override
    public void setShard(boolean isShard) {
        this.isShard = isShard;
        childPolicy.setShard(isShard);
    }

    @Override
    public void setDegraded(boolean isDegraded) {
        this.isDegraded = isDegraded;
        childPolicy.setDegraded(isDegraded);
    }

    @Override
    public void init(Cluster cluster, Collection<Host> hosts) {
        Set<Host> allHosts = cluster.getMetadata().getAllHosts();
        Set<Host> localHosts = new HashSet<>();
        this.cluster = cluster;
        for (Host host : allHosts) {
            String dc = host.getDatacenter();
            if (localDC.equals(dc)) {
                localHosts.add(host);
            }
        }
        childPolicy.init(cluster, localHosts);
    }

    @Override
    public HostDistance distance(Host host) {
        return childPolicy.distance(host);
    }

    @Override
    public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {
        ByteBuffer byteBuffer = statement.getRoutingKey(ProtocolVersion.V4, CodecRegistry.DEFAULT_INSTANCE);
        String keyspace = statement.getKeyspace();
        Set<Host> replicas = cluster.getMetadata().getReplicas(loggedKeyspace, byteBuffer);

        if (byteBuffer == null || keyspace == null) {
            return childPolicy.newQueryPlan(loggedKeyspace, statement);
        }

        return null;
    }

    @Override
    public void onAdd(Host host) {
        childPolicy.onAdd(host);
    }

    @Override
    public void onUp(Host host) {
        childPolicy.onUp(host);
    }

    @Override
    public void onDown(Host host) {
        childPolicy.onDown(host);
    }

    @Override
    public void onRemove(Host host) {
        onDown(host);
    }

    @Override
    public void close() {

    }

    private class HostIterator extends AbstractIterator<Host> {
        private Statement statement;
        private Iterator<Host> iterator;
        private Set<Host> replicas;
        private String loggedKeyspace;

        public HostIterator(Statement statement, Iterator<Host> iterator, String loggedKeyspace, Set<Host> replicas) {
            this.statement = statement;
            this.iterator = iterator;
            this.loggedKeyspace = loggedKeyspace;
            this.replicas = replicas;
        }

        @Override
        protected Host computeNext() {
            String expectDC = null;
            if (isDegraded) {
                statement.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
                statement.setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL);
            }

            //isDispatch 是否双写模式
            if (isDispatch(statement)) {
               // expectDC = loadbalance.selectDC()
                while(iterator.hasNext()){
                    Host host=iterator.next();
                    if(host.isUp()&&host.getDatacenter().equals(expectDC)){
                        return host;
                    }
                }
            }else {
                while (iterator.hasNext()){
                    Host host=iterator.next();
                    String dc=host.getDatacenter();
                    if (host.isUp()&&localDC.equals(dc)||"".equals(dc)){
                        return host;
                    }
                }
            }
            return null;
        }

    }

    private boolean isDispatch(Statement statement) {
        //return !statement.getConsistencyLevel().isSerial() && !isDegraded && isShard;
        return false;
    }
}
