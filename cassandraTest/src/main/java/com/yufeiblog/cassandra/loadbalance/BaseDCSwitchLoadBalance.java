package com.yufeiblog.cassandra.loadbalance;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.List;
import java.util.Map;

public abstract class BaseDCSwitchLoadBalance implements DCSwitchLoadbalance {
    protected static final HashFunction HASH_FUNCTION= Hashing.murmur3_32();
    @Override
    public String selectDC(String tableName, List<String> dcNames, Map<String, Object> pk) {
        String dcName=directDispatch(pk);
        if(dcName!=null){
            return dcName;
        }
        return dispatchByPK(tableName, dcNames, pk);
    }

    /**
     * 根据 pk 是否在特定的 PK 范围去选择发送的节点，即特定的 PK 发往特定 DC
     * @param pk 分区主键
     * @return 发送的DC
     */
    private String directDispatch(Map<String, Object> pk){
        return null;
    }

    /**
     *
     * @param tableName
     * @param dcNames
     * @param pk
     * @return
     */
    protected abstract String  dispatchByPK(String tableName, List<String> dcNames, Map<String, Object> pk);
}
