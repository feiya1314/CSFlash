package com.yufeiblog.cassandra.common;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yufeiblog.cassandra.exception.CSException;

import java.io.IOException;
import java.io.Serializable;

abstract class Cursor implements Serializable {
    protected static char CASSANDRA_CURSOR = '0';

    public abstract String cursorToString();

    public static Cursor convertToCursor(String cursor) {
        char prefix = cursor.charAt(0);
        Cursor newCursor = null;

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
        String realCursor = String.copyValueOf(cursor.toCharArray(), 1, cursor.length() - 1);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            newCursor = objectMapper.readValue(realCursor, Cursor.class);
        } catch (IOException  e){
            throw new CSException("invalid cursor,convert to cursor failed");
        }

        return newCursor;
    }

   /* private Cursor getCursorFromString(Cursor  ){

    }*/
}
