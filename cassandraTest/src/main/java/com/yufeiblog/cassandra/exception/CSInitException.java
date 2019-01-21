package com.yufeiblog.cassandra.exception;

public class CSInitException extends RuntimeException{
    public CSInitException(){
        super();
    }
    public CSInitException(String msg){
        super(msg);
    }

    public CSInitException(String message, Throwable cause){
        super(message,cause);
    }
}
