package com.yufeiblog.cassandra.dcmonitor;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.ClientBuilder;
import com.coreos.jetcd.Watch;
import com.yufeiblog.cassandra.common.EtcdConfiguraion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtcdClientFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(EtcdClientFactory.class);

    private EtcdConfiguraion etcdConfiguraion;
    private Client etcdClient;
    private ClientBuilder etcdClientBuilder;
    private Watch watch;

    public EtcdClientFactory(EtcdConfiguraion etcdConfiguraion) {
        this.etcdConfiguraion = etcdConfiguraion;
    }

    public void build() {
        if (etcdClient == null) {
            etcdClientBuilder = Client.builder();
            etcdClientBuilder.endpoints(etcdConfiguraion.getEctdHosts());
            etcdClient = etcdClientBuilder.build();
            watch = etcdClient.getWatchClient();
        }
    }

    public Client getEtcdClient() {
        return etcdClient;
    }

    public Watch getWatchClient() {
        return watch;
    }


}
