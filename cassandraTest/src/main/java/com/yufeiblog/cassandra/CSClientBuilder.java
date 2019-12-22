package com.yufeiblog.cassandra;

import com.yufeiblog.cassandra.async.handler.AsyncRequestHandler;
import com.yufeiblog.cassandra.common.ClientListener;
import com.yufeiblog.cassandra.dcmonitor.EtcdClientFactory;
import com.yufeiblog.cassandra.service.AsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

public class CSClientBuilder {
    private static Logger LOGGER = LoggerFactory.getLogger(CSClientBuilder.class);
    private List<ClientListener> listeners;
    private List<AsyncRequestHandler> handlers;
    public CSClientBuilder(){}
    public CSClientBuilder instance(){
        return new CSClientBuilder();
    }

    public synchronized void init(){

        ServiceLoader<ClientListener> serviceLoader = ServiceLoader.load(ClientListener.class);
        for (ClientListener clientListener :serviceLoader){
            listeners.add(clientListener);
        }
        //之后
        //	builder读本地配置，configmanager把配置读到本地配置类，在configmanager 中初始化configwatcher
        //configwatcher从配置中心取值,并监控配置中心，回调通知configmanager(实现configchangelistener),	configmanager把读到的配置刷到各个配置类

        //之后
        //初始化sessionmanager 也会初始化tableManager (	tablemanager主要是定时刷新业务表结构，如果modifytime有变动则刷新表结构缓存并让statmentcache失效)
        // 在其中初始化cs和双云监控（监控中主要创建双云文件、一分钟检查一次，有改变则回调sessionmanager(实现doublecloudlistener)刷新当前活跃dc同时doublecloudwatcher会开启一个线程用于监控etcd中双云的情况
        // 变化后刷新到双云文件并回调sessionmanager）
        //之后通过serviceloader初始化requesthandler<REQUEST extend asyncrequest,RESPONSE> initHandlers();


    }

    private void initHandlers(){
        ServiceLoader<AsyncRequestHandler> serviceLoader = ServiceLoader.load(AsyncRequestHandler.class);
        for (AsyncRequestHandler handler :serviceLoader){
            handlers.add(handler);
        }
    }

    public AsyncClient build(){
        //	client中有sessionmanager  configmanager listener  tablemanager  exceptionhandler  requesthandlermap  timeoutcompletablefuture
        return  new DefaultAsyncClient();
    }
}
