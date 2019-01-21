package com.yufeiblog.cassandra.common.interceptor;

public interface InterceptionListener {

    default void init(){}

    default void beforeRequest(){}

    default void afterRequest(){}
}
