package com.yufeiblog.cassandra.common;

public class Condition {
    public static String COMPARTOR_EQ = "=";
    public static String COMPARTOR_NEQ = "!=";
    public static String COMPARTOR_GT = ">";
    public static String COMPARTOR_LT = "<";
    public static String COMPARTOR_LEQ = "<=";
    public static String COMPARTOR_GEQ = ">=";

    private String columnName;
    private Object value;
    private String operator;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}

