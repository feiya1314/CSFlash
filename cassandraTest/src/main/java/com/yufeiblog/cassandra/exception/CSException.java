package com.yufeiblog.cassandra.exception;

public class CSException extends RuntimeException {
    public CSException(){
        super();
    }
    public CSException(String msg){
        super(msg);
    }

    public CSException(String message, Throwable cause){
        super(message,cause);
    }
}
