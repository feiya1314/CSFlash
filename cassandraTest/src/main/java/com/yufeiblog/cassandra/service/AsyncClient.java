package com.yufeiblog.cassandra.service;

import com.yufeiblog.cassandra.async.*;

import java.util.concurrent.CompletableFuture;

public interface  AsyncClient {

    default CompletableFuture saveSync(SaveRequest saveRequest){
        return execute(saveRequest);
    }

    default CompletableFuture deleteSync(DeleteRequest deleteRequest){
        return execute(deleteRequest);
    }

    default CompletableFuture<ResultSet> querySync(QueryRequest queryRequest){
        return execute(queryRequest);
    }

    <REQUEST extends AsyncRequest, RESPONSE> CompletableFuture<RESPONSE>  execute(REQUEST request);
}
