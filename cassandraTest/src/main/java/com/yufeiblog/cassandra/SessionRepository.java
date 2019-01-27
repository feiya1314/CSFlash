package com.yufeiblog.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.yufeiblog.cassandra.common.CassandraConfiguration;
import com.yufeiblog.cassandra.dcmonitor.DCStatus;
import com.yufeiblog.cassandra.dcmonitor.DCStatusListener;
import com.yufeiblog.cassandra.loadbalance.SwitchLoadbalancePolicy;
import com.yufeiblog.cassandra.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRepository implements DCStatusListener {
    /*    private SwitchLoadbalancePolicy loadbalancePolicy;*/
    private Cluster cluster;
    private CassandraConfiguration configuration;
    private Session defaultSession;
    private Map<String, Object> replication;
    private Map<Integer, Session> sessionCache = new HashMap<>();
    protected Map<String, PreparedStatement> statementCache = new ConcurrentHashMap<>();

    protected SessionRepository(Cluster cluster, CassandraConfiguration configuration, Map<String, Object> replication) {
        this.cluster = cluster;
        this.configuration = configuration;
        this.replication = replication;
    }


    public PreparedStatement prepareStatement(int appId, String sql) {
        PreparedStatement statement = statementCache.get(sql);
        if (statement == null) {
            statement = getSession(appId).prepare(sql);
            statementCache.put(sql, statement);
        }
        return statement;
    }

    @Override
    public void notifyClient(DCStatus dcStatus) {
        //loadbalancePolicy
        LoadBalancingPolicy loadBalancingPolicy = cluster.getConfiguration().getPolicies().getLoadBalancingPolicy();
        if (loadBalancingPolicy instanceof SwitchLoadbalancePolicy) {
            //todo switch
            String activeDC = dcStatus.getActiveDC();
            ((SwitchLoadbalancePolicy) loadBalancingPolicy).setLoaclDC(activeDC);
        }
        cluster.getConfiguration().getPoolingOptions().refreshConnectedHosts();
    }

    public Session getSession() {
        synchronized (this) {
            if (defaultSession == null) {
                defaultSession = cluster.connect();
            }
        }
        return defaultSession;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Map<String, Object> getReplication() {
        return replication;
    }

    public Session getSession(int appId) {
        Session session;
        synchronized (sessionCache) {
            session = sessionCache.get(appId);
            if (session == null) {
                session = cluster.connect(Utils.getKeyspace(appId));
            }
        }
        return session;
    }

    public void close() {
        if (sessionCache != null) {
            for (Session session : sessionCache.values()) {
                session.close();
                session = null;
            }
        }
        cluster.close();
        cluster = null;
    }

}
