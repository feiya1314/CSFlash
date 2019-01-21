package com.yufeiblog.cassandra.result;

import com.yufeiblog.cassandra.common.Constant;
import com.yufeiblog.cassandra.common.ReturnCode;

public class Result {
    private String returnCode;
    private String returnMsg;
    public boolean isSuccess(){
        if(ReturnCode.SUCCESS.equals(returnCode)){
            return true;
        }
        return false;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}
