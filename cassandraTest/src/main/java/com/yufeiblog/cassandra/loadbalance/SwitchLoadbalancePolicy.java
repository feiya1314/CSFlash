package com.yufeiblog.cassandra.loadbalance;

import com.datastax.driver.core.policies.LoadBalancingPolicy;

public interface SwitchLoadbalancePolicy extends LoadBalancingPolicy {
     void setLoaclDC(String dc);

     void setShard(boolean isShard);

     void setDegraded(boolean isDegraded);
}
