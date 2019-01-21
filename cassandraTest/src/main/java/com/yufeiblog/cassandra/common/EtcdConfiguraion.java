package com.yufeiblog.cassandra.common;

public class EtcdConfiguraion {
    private String[] ectdHosts;
    private String clusterName;
    private String serviceName;
    private String currentDC;
    private String dc1;
    private String dc2;

    public String[] getEctdHosts() {
        return ectdHosts;
    }

    public void setEctdHosts(String[] ectdHosts) {
        this.ectdHosts = ectdHosts;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCurrentDC() {
        return currentDC;
    }

    public void setCurrentDC(String currentDC) {
        this.currentDC = currentDC;
    }

    public String getDc1() {
        return dc1;
    }

    public void setDc1(String dc1) {
        this.dc1 = dc1;
    }

    public String getDc2() {
        return dc2;
    }

    public void setDc2(String dc2) {
        this.dc2 = dc2;
    }
}
