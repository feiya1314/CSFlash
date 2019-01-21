package com.yufeiblog.cassandra.loadbalance;

import java.util.List;
import java.util.Map;

public interface DCSwitchLoadbalance {
    String selectDC(String tableName, List<String> dcNames, Map<String, Object> pk);
}
