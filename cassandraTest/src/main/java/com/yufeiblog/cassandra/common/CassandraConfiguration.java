package com.yufeiblog.cassandra.common;

public class CassandraConfiguration {
    private String contactPoint ;
    private String cassandraUserName;
    private String cassandraPassword;
    private int port = 9042;
    private String replicate;

    public String getContactPoint() {
        return contactPoint;
    }

    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    public String getCassandraUserName() {
        return cassandraUserName;
    }

    public void setCassandraUserName(String cassandraUserName) {
        this.cassandraUserName = cassandraUserName;
    }

    public String getCassandraPassword() {
        return cassandraPassword;
    }

    public void setCassandraPassword(String cassandraPassword) {
        this.cassandraPassword = cassandraPassword;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getReplicate() {
        return replicate;
    }

    public void setReplicate(String replicate) {
        this.replicate = replicate;
    }
}
