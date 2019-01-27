package com.yufeiblog.cassandra.common;

import com.datastax.driver.core.PagingState;

import java.io.IOException;
import java.io.Serializable;

public class PagingStateCursor extends Cursor implements Serializable {
    private PagingState pagingState;
    private String test = "abcedefg";

    @Override
    public String cursorToString() {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(this);
        }catch (IOException e){
            System.out.println("cursorToString exception"+e.getMessage());
        }
        return CASSANDRA_CURSOR+json;
    }

    public static void main(String[] args) {
        Cursor cursor = new PagingStateCursor();
        String json = cursor.cursorToString();
        Cursor cursor1 = Cursor.convertToCursor(json);
        System.out.println(json);
    }

    public PagingState getPagingState() {
        return pagingState;
    }

    public void setPagingState(PagingState pagingState) {
        this.pagingState = pagingState;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
