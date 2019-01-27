package com.yufeiblog.cassandra.model;

public class TableDefination {
    private int partitionKeyCount;
    private Column[] columns;
    private String[] primaryKeys;
    private Index[] indexes;
    private String clusterOrder;

    public int getPartitionKeyCount() {
        return partitionKeyCount;
    }

    public void setPartitionKeyCount(int partitionKeyCount) {
        this.partitionKeyCount = partitionKeyCount;
    }

    public Column[] getColumns() {
        return columns;
    }

    public void setColumns(Column[] columns) {
        this.columns = columns;
    }

    public String[] getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(String[] primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public Index[] getIndexes() {
        return indexes;
    }

    public void setIndexes(Index[] indexes) {
        this.indexes = indexes;
    }

    public String getClusterOrder() {
        return clusterOrder;
    }

    public void setClusterOrder(String clusterOrder) {
        this.clusterOrder = clusterOrder;
    }
}
