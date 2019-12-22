package com.yufeiblog.cassandra;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.yufeiblog.cassandra.async.AsyncRequest;
import com.yufeiblog.cassandra.common.ClientListener;
import com.yufeiblog.cassandra.service.AsyncClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DefaultAsyncClient implements AsyncClient, ClientListener {
    private Session session;
    private List<ClientListener> listeners;
    @Override
    public <REQUEST extends AsyncRequest, RESPONSE> CompletableFuture<RESPONSE> execute(REQUEST request) {

        Insert insert = QueryBuilder.insertInto("");
        beforeRequest();
        ResultSetFuture resultSetFuture = session.executeAsync(insert);
        afterRequest();
        //	processresponse主要是处理cs返回的东西
        //	context中原始响应和异常会存放在此
        //	返回resultsetfuture
        //	之后addlistener  其中resultsetfuture.get
        //	    response =requesthandler.processresponse(context,resultset)
        //
        //	如果失败则catch ，并将异常转化放入context

        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void beforeRequest() {
        for (ClientListener listener : listeners){
            listener.beforeRequest();
        }
    }

    @Override
    public void afterRequest() {

    }
}
