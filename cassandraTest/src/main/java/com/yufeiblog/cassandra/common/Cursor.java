package com.yufeiblog.cassandra.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yufeiblog.cassandra.exception.CSException;
import com.yufeiblog.cassandra.utils.Utils;

import java.io.IOException;
import java.io.Serializable;

public abstract class Cursor implements Serializable {
    protected static char CASSANDRA_CURSOR = '0';
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    public abstract String cursorToString();

    public static Cursor convertToCursor(String cursor) {
        if (Utils.isStringEmpty(cursor)){
            return null;
        }
        char prefix = cursor.charAt(0);
        Cursor newCursor = null;
        String realCursor = String.copyValueOf(cursor.toCharArray(), 1, cursor.length() - 1);
        try {
            switch (prefix) {
                case '0':
                    // newCursor = new PagingStateCursor();
                    newCursor = objectMapper.readValue(realCursor, PagingStateCursor.class);
                    break;
                case '1':
                    //newCursor = new ESCursor();
                    break;
                default:
                    throw new CSException("invalid cursor");
            }

        } catch (IOException e) {
            throw new CSException("invalid cursor,convert to cursor failed " + e.getMessage());
        }

        return newCursor;
    }

   /* private Cursor getCursorFromString(Cursor  ){

    }*/
}
