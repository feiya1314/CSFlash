package com.yufeiblog.cassandra.common;

public interface ClientListener {
    default void init(){};

    default void beforeRequest(){};

    default void afterRequest(){};
}
