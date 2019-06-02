package com.yufeiblog.cassandra.test;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.util.concurrent.ListenableFuture;
import com.yufeiblog.cassandra.utils.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AsyncTest extends BaseTest{
    @Override
    protected void test() {
        Select select = QueryBuilder.select().from(Utils.getKeyspace(appId),tableName);
        select.where(QueryBuilder.eq("uid","41"));//.and(QueryBuilder.eq("prim1","prim1_410"));
        //Statement statement = new SimpleStatement();
        ResultSetFuture resultSetFuture = session.executeAsync(select);
        try {
            ResultSet resultSet = resultSetFuture.get();
            resultSet.isExhausted();
            //ListenableFuture<ResultSet>  resultSetListenableFuture = resultSet.fetchMoreResults();
            Iterator<Row> resultSetIterator = resultSet.iterator();
            while (resultSetIterator.hasNext()){
                Row row = resultSetIterator.next();
                Map<String,Object> result = getResult(row);
                System.out.println(result);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Map<String,Object> getResult(Row row){
        Map<String,Object> result = new HashMap<>();
        ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
        List<ColumnDefinitions.Definition> definitions = columnDefinitions.asList();
        for (ColumnDefinitions.Definition definition : definitions){
            String column = definition.getName();
            Class c = definition.getType().getClass();
            Object value = row.getObject(column);
            result.put(column,value);
        }
        return result;
    }

    public static void main(String[] args) {
        new AsyncTest().execute();
    }
}
