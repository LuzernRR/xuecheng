package com.lxz.base.exception;

import java.io.Serializable;

/**
 * @description:和前端约定返回的异常信息模型
 * @author: 12860
 * @time: 2024/8/3 下午4:43
 */
public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }
    public String getErrMessage() {
        return errMessage;
    }
    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
