package com.yufei.test.jmx.mbean;

public interface ConfigMBean {
    boolean isEnableThrift();

    void setEnableThrift(boolean enableThrift);

    String getName();

    void setName(String name);

    int getPeriod();

    void setPeriod(int period);

    String getAddress();

    void setAddress(String address);
}
