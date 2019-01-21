package com.yufeiblog.cassandra.utils;

public final class Utils {
    public static String getKeyspace(int appId){
        return "app"+appId;
    }
}
