package com.yufeiblog.cassandra;

import com.yufeiblog.cassandra.dcmonitor.EtcdClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSClientBuilder {
    private static Logger LOGGER = LoggerFactory.getLogger(CSClientBuilder.class);
    public CSClientBuilder(){}
    public CSClientBuilder instance(){
        return new CSClientBuilder();
    }

    public synchronized void init(){

    }
}
