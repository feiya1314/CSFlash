package com.yufeiblog.cassandra.common;

import com.yufeiblog.cassandra.exception.CSException;

import java.io.Serializable;

abstract class Cursor implements Serializable {
    protected static char CASSANDRA_CURSOR = '0';

    public abstract String cursorToString();

    public static Cursor convertToCursor(String cursor) {
        char prefix = cursor.charAt(0);
        Cursor newCursor = null;
        String realCursor = String.copyValueOf(cursor.toCharArray(),1,cursor.length()-1);
        switch (prefix) {
            case '0':
                newCursor = new PagingStateCursor();
                break;
            case '1':
                //newCursor = new ESCursor();
                break;
            default:
                throw new CSException("invalid cursor");
        }

        return newCursor;
    }

   /* private Cursor getCursorFromString(Cursor  ){

    }*/
}
