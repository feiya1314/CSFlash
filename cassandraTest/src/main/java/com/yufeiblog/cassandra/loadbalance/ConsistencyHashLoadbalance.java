package com.yufeiblog.cassandra.loadbalance;

import com.google.common.hash.HashCode;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistencyHashLoadbalance extends BaseDCSwitchLoadBalance {
    private volatile SortedMap<Integer,String> hashRing;
    public ConsistencyHashLoadbalance(Map<String,Integer> weightMap){
        hashRing = init(weightMap);
    }
    private SortedMap<Integer,String> init(Map<String,Integer> weightMap){
        SortedMap<Integer,String> tempHashRing=new TreeMap<>();
        for(Map.Entry<String,Integer> entry:weightMap.entrySet()){
            int replicaNums=entry.getValue();
            String dcName=entry.getKey();
            for (int i = 0; i <replicaNums ; i++) {
                String replicaName = dcName+"replicaHost_"+i;
                HashCode hashCode = HASH_FUNCTION.hashBytes(replicaName.getBytes(Charset.forName("UTF-8")));
                tempHashRing.put(hashCode.asInt(),dcName);
            }
        }
        return tempHashRing;
    }
    @Override
    protected String dispatchByPK(String tableName, List<String> dcNames, Map<String, Object> pk) {
        StringBuilder sb =new StringBuilder();
        for(Map.Entry<String,Object> entry:pk.entrySet()){
            sb.append(entry.getValue());
        }
        String dcName = getDC(sb.toString().getBytes());
        return dcName;
    }

    private String getDC(byte[] pk){
        HashCode hashCode=HASH_FUNCTION.hashBytes(pk);
        int hashInt=hashCode.asInt();
        if(!hashRing.containsKey(hashInt)){
            SortedMap<Integer,String> partition = hashRing.tailMap(hashInt);
            hashInt=partition.isEmpty()?hashRing.firstKey():partition.firstKey();
        }
        return hashRing.get(hashInt);
    }
}
