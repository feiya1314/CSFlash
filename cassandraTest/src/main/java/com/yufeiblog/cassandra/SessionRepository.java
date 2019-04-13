package com.yufeiblog.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.yufeiblog.cassandra.common.CassandraConfiguration;
import com.yufeiblog.cassandra.dcmonitor.DCStatus;
import com.yufeiblog.cassandra.dcmonitor.DCStatusListener;
import com.yufeiblog.cassandra.loadbalance.SwitchLoadbalancePolicy;
import com.yufeiblog.cassandra.model.Column;
import com.yufeiblog.cassandra.model.TableDefination;
import com.yufeiblog.cassandra.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private Map<String, TableDefination> tableDefinationMap = new ConcurrentHashMap<>();

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

    void selectByToken(String pkName, TokenRange tokenRange,String[] columns, String keyspace, String table){

        Statement statement = QueryBuilder.select(columns).from(keyspace,table)
                .where(QueryBuilder.gt(QueryBuilder.token(pkName),tokenRange.getStart().getValue()))
                .and(QueryBuilder.lte(QueryBuilder.token(pkName),tokenRange.getEnd().getValue()));
        ResultSet resultSet = defaultSession.execute(statement);

    }
    void selectPkCount(TokenRange tokenRange,String[] columns, String keyspace, String table){
        Statement statement = QueryBuilder.select("partitions_count").from("system","size_estimates")
                .where(QueryBuilder.gt("keyspacename",keyspace))
                .and(QueryBuilder.gt("tablename",table))
                .and(QueryBuilder.gt("rangestart",tokenRange.getStart().getValue()))
                .and(QueryBuilder.gt("rangestart",tokenRange.getEnd().getValue()));
        ResultSet resultSet = defaultSession.execute(statement);
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

    public TableDefination getTableDefination(int appId, String tableName) {
        TableDefination tableDefination = tableDefinationMap.get(buildTableDefinationKey(appId,tableName));
        if (tableDefination == null) {
            tableDefination = new TableDefination();
            TableMetadata tableMetadata = cluster.getMetadata().getKeyspace(Utils.getKeyspace(appId)).getTable(tableName);
            return getTableDefinationFromMeta(tableMetadata);
        }

        return tableDefination;
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

    private String buildTableDefinationKey(int appId, String tableName) {
        return appId + "_" + tableName;
    }

    private TableDefination getTableDefinationFromMeta(TableMetadata tableMetadata){
        TableDefination tableDefination = new TableDefination();
        List<ColumnMetadata> pks = tableMetadata.getPartitionKey();
        List<ColumnMetadata> primaryKeys = tableMetadata.getPrimaryKey();
        int size = pks.size()+primaryKeys.size();
        String[] primary = new String[size];
        List<ColumnMetadata> allPrimaryKeys = new ArrayList<>();
        allPrimaryKeys.addAll(pks);
        allPrimaryKeys.addAll(primaryKeys);
        int i =0 ;
        for (ColumnMetadata columnMetadata : allPrimaryKeys){
            primary[i++] = columnMetadata.getName();
        }
        tableDefination.setPrimaryKeys(primary);
        tableDefination.setPartitionKeyCount(pks.size());

        List<ColumnMetadata> columns = tableMetadata.getColumns();
        Column[] targetColumns = new Column[columns.size()];
        i=0;
        for (ColumnMetadata columnMetadata:columns){
            targetColumns[i] = new Column();
            targetColumns[i].setColumnName(columnMetadata.getName());
            DataType type = columnMetadata.getType();
            targetColumns[i].setType(type.getName().name());
            i++;
        }
        tableDefination.setColumns(targetColumns);
        List<ClusteringOrder> clusteringOrders =  tableMetadata.getClusteringOrder();
        return tableDefination;
    }
}
