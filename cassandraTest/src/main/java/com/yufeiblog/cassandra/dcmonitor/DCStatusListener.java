package com.yufeiblog.cassandra.dcmonitor;

public interface DCStatusListener {
     void notifyClient(DCStatus dcStatus);
}
