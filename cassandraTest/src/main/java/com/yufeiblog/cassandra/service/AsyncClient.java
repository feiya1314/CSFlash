package com.yufeiblog.cassandra.service;

import com.yufeiblog.cassandra.async.AsyncRequest;

import java.util.concurrent.CompletableFuture;

public interface  AsyncClient {


    <REQUEST extends AsyncRequest, RESPONSE> CompletableFuture<RESPONSE>  execute(REQUEST request, RESPONSE response);
}
