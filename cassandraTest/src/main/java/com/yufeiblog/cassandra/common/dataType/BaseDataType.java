package com.yufeiblog.cassandra.common.dataType;

import com.datastax.driver.core.DataType;
import com.yufeiblog.cassandra.exception.CSException;

public abstract class BaseDataType {
    public abstract Object getValueFromUser(String column, Object value);

    public abstract DataType getCsDataType();

    public abstract Object formatUserValue(Object aftergetvaluefromuser);

    public abstract Object getValueFromCs(String column, Object value);
    public abstract Object getEsValue(String column, Object value);

    public abstract String getWrapperType();

    public String getEsType()
    {
        throw new CSException("not support");
    }


}


