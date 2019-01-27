package com.yufeiblog.cassandra.utils;

import java.util.Collection;

public final class Utils {
    public static String getKeyspace(int appId) {
        return "app" + appId;
    }

    public static boolean isArrayEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isCollectionsEmpty(Collection collection){
        return collection == null || collection.isEmpty();
    }
}
