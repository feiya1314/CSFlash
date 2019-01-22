package com.yufeiblog.cassandra.common.dataType;

import com.datastax.driver.core.DataType;

public class StringType extends BaseDataType {
    @Override
    public Object getValueFromUser(String column, Object value) {
        return null;
    }

    @Override
    public DataType getCsDataType() {
        return null;
    }

    @Override
    public Object formatUserValue(Object aftergetvaluefromuser) {
        return null;
    }

    @Override
    public Object getValueFromCs(String column, Object value) {
        return null;
    }

    @Override
    public Object getEsValue(String column, Object value) {
        return null;
    }

    @Override
    public String getWrapperType() {
        return null;
    }
}
