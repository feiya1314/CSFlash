package com.yufeiblog.cassandra.utils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;

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

    public static boolean isStringEmpty(String str){
        return str == null || str.isEmpty();
    }

    public static String getFilePath(String path){
        String file = path;
        if (!path.startsWith("/")){
            file = "/" + path;
        }
        URL url = Utils.class.getResource(file);
        if (url == null){
            return path;
        }
       try {
           file = url.toURI().getPath();
       }catch (Exception e){

       }
       return file;
    }

    public static void main(String[] args) {
        //Utils.loadProperties("G:\\CSFlash\\cassandraTest\\src\\main\\resources\\config.properties")
        Properties properties = new Properties();
        String path = getFilePath("G:\\CSFlash\\cassandraTest\\src\\main\\resources\\config.properties");
        try {
            File file = new File(path);
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            properties.load(reader);
        }catch (Exception e){

        }
        System.out.println(properties);
    }
}
