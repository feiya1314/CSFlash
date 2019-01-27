package com.yufeiblog.cassandra.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.yufeiblog.cassandra.SessionManager;
import com.yufeiblog.cassandra.SessionRepository;
import com.yufeiblog.cassandra.model.Column;
import com.yufeiblog.cassandra.model.TableDefination;
import com.yufeiblog.cassandra.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseService {
    protected String createUser = "CREATE USER %s WITH PASSWORD '%s' NOSUPERUSER";
    protected String grantPermissionSelect = "GRANT SELECT ON ALL KEYSPACES TO '%s'";
    protected String grantPermissionModify = "GRANT MODIFY ON ALL KEYSPACES TO '%s'";

    protected SessionRepository sessionRepository;
    protected Map<String, TableDefination> tableDefinationMap = new ConcurrentHashMap<>();

    public BaseService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }



    protected boolean isKeyspaceExist(int appId) {
        KeyspaceMetadata keyspaceMetadata = sessionRepository.getSession().getCluster().getMetadata().getKeyspace(Utils.getKeyspace(appId));
        return keyspaceMetadata != null;
    }

    protected boolean isTableExist(int appId, String tableName) {
        TableMetadata tableMetadata = sessionRepository.getSession().getCluster().getMetadata().getKeyspace(Utils.getKeyspace(appId)).getTable(tableName);
        return tableMetadata != null;
    }

    protected TableDefination getTableDefination(int appId, String tableName) {
        TableDefination tableDefination = tableDefinationMap.get(buildTableDefinationKey(appId,tableName));
        if (tableDefination == null) {
            tableDefination = new TableDefination();
            TableMetadata tableMetadata = sessionRepository.getCluster().getMetadata().getKeyspace(Utils.getKeyspace(appId)).getTable(tableName);
            return getTableDefinationFromMeta(tableMetadata);
        }

        return tableDefination;
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
