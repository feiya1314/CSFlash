package com.yufeiblog.cassandra.dcmonitor;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.ClientBuilder;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.watch.WatchEvent;
import com.coreos.jetcd.watch.WatchResponse;
import com.yufeiblog.cassandra.common.EtcdConfiguraion;
import com.yufeiblog.cassandra.SessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DCStatusWatcher {
    private static Logger LOGGER = LoggerFactory.getLogger(EtcdClientFactory.class);
    private DCStatusListener dcStatusListener;
    private static final String ETCD_WATCH_DIR = "/cassandra/default";
    private static final String ACTIVE_DC = "activeDC";
    private EtcdConfiguraion etcdConfiguraion;
    private boolean enable = true;
    private EtcdClientFactory etcdClientFactory;
    private Watch watch;
    private volatile boolean keepWatch = true;
    private volatile String previousStatus;
    private DCStatus dcStatus = new DCStatus();


    public DCStatusWatcher(DCStatusListener dcStatusListener, EtcdConfiguraion etcdConfiguraion) {
        this.dcStatusListener = dcStatusListener;
        this.etcdConfiguraion = etcdConfiguraion;
        previousStatus = etcdConfiguraion.getCurrentDC();
    }

    public void init() {
        if (enable) {
            LOGGER.info("dc switch enable");
            etcdClientFactory = new EtcdClientFactory(etcdConfiguraion);
            etcdClientFactory.build();
            watch = etcdClientFactory.getWatchClient();
            String watchDir = StringUtils.join(ETCD_WATCH_DIR,etcdConfiguraion.getServiceName(),etcdConfiguraion.getClusterName(),ACTIVE_DC,"/");
            LOGGER.info("watch dir is : {}",watchDir);
            watch(watchDir);
        }
    }

    public Client getEtcdClient() {
        return etcdClientFactory.getEtcdClient();
    }

    private void watch(String watchDir) {
        ByteSequence byteSequence = new ByteSequence(watchDir);
        Watch.Watcher watcher = watch.watch(byteSequence);

        while (keepWatch) {
            LOGGER.info("etcd start watch {}", watchDir);
            WatchResponse watchResponse = null;
            try {
                watchResponse = watcher.listen();
            } catch (InterruptedException e) {
                LOGGER.error("etcd start watch was interrupted ", e);
                keepWatch = false;
                watch.close();
                //Thread.currentThread().interrupt();
            }
            if (watchResponse != null) {
                List<WatchEvent> watchEvents = watchResponse.getEvents();
                for (WatchEvent watchEvent : watchEvents) {
                    if (watchEvent.getEventType() == WatchEvent.EventType.PUT) {
                        KeyValue keyValue = watchEvent.getKeyValue();
                        String activeDC = keyValue.getValue().toStringUtf8();

                        if (!previousStatus.equals(activeDC)){
                            LOGGER.info("watch etcd active dc changed ,activeDC:{}",activeDC);
                            previousStatus = activeDC;
                            dcStatus.setActiveDC(activeDC);
                            dcStatusListener.notifyClient(dcStatus);
                        }
                        LOGGER.info("watch etcd active dc changed ,but same as previous status : {}",previousStatus);

                    }

                    if (watchEvent.getEventType() == WatchEvent.EventType.DELETE){
                        LOGGER.warn("watch etcd active dir was deleted");
                    }
                }
            } else {
                LOGGER.warn("watch response is null");
            }
        }
    }
}

