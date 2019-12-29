package com.yufei.test.jmx.mbean;

public class Config implements ConfigMBean{
    private boolean enableThrift;
    private String name;
    private int period;
    private String address;

    public boolean isEnableThrift() {
        return enableThrift;
    }

    public void setEnableThrift(boolean enableThrift) {
        this.enableThrift = enableThrift;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
