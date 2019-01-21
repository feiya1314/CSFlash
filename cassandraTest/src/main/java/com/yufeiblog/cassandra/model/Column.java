package com.yufeiblog.cassandra.model;

import java.io.Serializable;

public class Column implements Serializable {
    private String columnName;
    private String type;
    private String describtion;

    public Column(){}
    public Column(String columnName,String type){
        this.columnName=columnName;
        this.type=type;
    }
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescribtion() {
        return describtion;
    }

    public void setDescribtion(String describtion) {
        this.describtion = describtion;
    }
}
