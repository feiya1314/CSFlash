package com.yufeiblog.cassandra;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.yufeiblog.cassandra.async.AsyncRequest;
import com.yufeiblog.cassandra.service.AsyncClient;

import java.util.concurrent.CompletableFuture;

public class DefaultAsyncClient implements AsyncClient {
    private Session session;
    @Override
    public <REQUEST extends AsyncRequest, RESPONSE> CompletableFuture<RESPONSE> execute(REQUEST request, RESPONSE response) {

        Insert insert = QueryBuilder.insertInto("");
        ResultSetFuture resultSetFuture = session.executeAsync(insert);
        return null;
    }
}
