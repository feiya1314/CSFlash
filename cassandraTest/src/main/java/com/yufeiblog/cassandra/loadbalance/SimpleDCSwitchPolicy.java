package com.yufeiblog.cassandra.loadbalance;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.util.*;

public class SimpleDCSwitchPolicy implements SwitchLoadbalancePolicy {

    private LoadBalancingPolicy childPolicy;
    private volatile String localDC;
    private final boolean shuffleReplicas = true;
    private volatile Metadata clusterMetadata;
    private volatile ProtocolVersion protocolVersion;
    private volatile CodecRegistry codecRegistry;
    private volatile boolean isShard = false;
    private volatile boolean isDegraded = false;

    public SimpleDCSwitchPolicy(LoadBalancingPolicy childPolicy) {
        if (childPolicy == null) {
            this.childPolicy = DCAwareRoundRobinPolicy.builder().build();
        }
        this.childPolicy = childPolicy;
    }

    @Override
    public void setLoaclDC(String dc) {
        this.localDC = dc;
    }

    @Override
    public void setShard(boolean isShard) {

    }

    @Override
    public void setDegraded(boolean isDegraded) {

    }

    @Override
    public void init(Cluster cluster, Collection<Host> hosts) {
        clusterMetadata = cluster.getMetadata();
        protocolVersion = cluster.getConfiguration().getProtocolOptions().getProtocolVersion();
        codecRegistry = cluster.getConfiguration().getCodecRegistry();

        Set<Host> allHosts = cluster.getMetadata().getAllHosts();
        Set<Host> localHosts = new HashSet<>();
        /*        this.cluster = cluster;*/
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
        String dc = host.getDatacenter();
        if (dc == null) {
            dc = localDC;
        }
        if ("".equals(dc) || dc.equals(localDC))
            return HostDistance.LOCAL;

        return HostDistance.IGNORED;
    }

    @Override
    public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {

        ByteBuffer partitionKey = statement.getRoutingKey(protocolVersion, codecRegistry);
        String keyspace = statement.getKeyspace();
        if (keyspace == null)
            keyspace = loggedKeyspace;

        if (partitionKey == null || keyspace == null)
            return childPolicy.newQueryPlan(keyspace, statement);

        final Set<Host> replicas = clusterMetadata.getReplicas(Metadata.quote(keyspace), partitionKey);
        if (replicas.isEmpty())
            return childPolicy.newQueryPlan(loggedKeyspace, statement);

        final Iterator<Host> iter;

        if (shuffleReplicas) {
            List<Host> l = Lists.newArrayList(replicas);
            Collections.shuffle(l);
            iter = l.iterator();
        } else {
            iter = replicas.iterator();
        }

        return new AbstractIterator<Host>() {
            @Override
            protected Host computeNext() {
                while (iter.hasNext()) {
                    Host host = iter.next();
                    String dc = host.getDatacenter();
                    if (host.isUp() && localDC.equals(dc)) {
                        return host;
                    }
                }
                return endOfData();
            }
        };
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
        childPolicy.onRemove(host);
    }

    @Override
    public void close() {

    }
}
