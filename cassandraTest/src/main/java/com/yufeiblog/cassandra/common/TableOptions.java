package com.yufeiblog.cassandra.common;

import com.datastax.driver.core.ConsistencyLevel;

public class TableOptions {
    private int ttl;
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.LOCAL_QUORUM;
    private String clusteringOrder = "ASC";
    private int partitionKeyCount = 1;

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public String getClusteringOrder() {
        return clusteringOrder;
    }

    public void setClusteringOrder(String clusteringOrder) {
        this.clusteringOrder = clusteringOrder;
    }

    public int getPartitionKeyCount() {
        return partitionKeyCount;
    }

    public void setPartitionKeyCount(int partitionKeyCount) {
        this.partitionKeyCount = partitionKeyCount;
    }
}
