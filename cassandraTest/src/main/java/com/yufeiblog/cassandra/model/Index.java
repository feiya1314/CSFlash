package com.yufeiblog.cassandra.model;

public class Index {
    private String indexName;
    private String indeType;
    private String describtion;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndeType() {
        return indeType;
    }

    public void setIndeType(String indeType) {
        this.indeType = indeType;
    }

    public String getDescribtion() {
        return describtion;
    }

    public void setDescribtion(String describtion) {
        this.describtion = describtion;
    }
}
