package com.yufeiblog.cassandra.test;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.watch.WatchEvent;
import com.coreos.jetcd.watch.WatchResponse;
import com.yufeiblog.cassandra.common.EtcdConfiguraion;
import com.yufeiblog.cassandra.dcmonitor.DCStatus;
import com.yufeiblog.cassandra.dcmonitor.DCStatusListener;
import com.yufeiblog.cassandra.dcmonitor.DCStatusWatcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EtcdTest {

    public EtcdListen builder() {
        return new EtcdListen();
    }

    public static void main(String[] args) throws Exception {
        String[] etcdhost = new String[1];
        etcdhost[0] = "http://192.168.3.9:2379";
        EtcdConfiguraion configuraion = new EtcdConfiguraion();
        configuraion.setEctdHosts(etcdhost);
        DCStatusWatcher watcher = new DCStatusWatcher(new EtcdTest().builder(), configuraion);
        watcher.init();
        Client etcdClient = watcher.getEtcdClient();
        KV kv = etcdClient.getKVClient();
        ByteSequence byteSequence = new ByteSequence("/cs/monitor");
        CompletableFuture<GetResponse> result =  kv.get(byteSequence);
        GetResponse response =  result.get();
        List<KeyValue> keyValues =  response.getKvs();
        for (KeyValue keyValue : keyValues){
            ByteSequence va =keyValue.getValue();
            String vvv = va.toStringUtf8();
            System.out.println(vvv);
        }

        Watch watch = etcdClient.getWatchClient();

        while (true) {
            Watch.Watcher watcher1 = watch.watch(byteSequence);
            WatchResponse watchResponse  = watcher1.listen();
            List<WatchEvent> watchEvents =  watchResponse.getEvents();

            for (WatchEvent watchEvent : watchEvents) {
                if (watchEvent.getEventType() == WatchEvent.EventType.PUT) {
                    System.out.println("new put");
                    KeyValue keyValue = watchEvent.getKeyValue();
                    KeyValue preKeyValue = watchEvent.getPrevKV();
                    String sss = keyValue.getValue().toStringUtf8();
                    String pre = preKeyValue.getValue().toStringUtf8();
                    System.out.println(sss);
                    System.out.println("pre "+pre);
                }
            }
        }
    }

    private class EtcdListen implements DCStatusListener {
        @Override
        public void notifyClient(DCStatus dcStatus) {
            System.out.println(dcStatus.getActiveDC());
        }
    }
}
