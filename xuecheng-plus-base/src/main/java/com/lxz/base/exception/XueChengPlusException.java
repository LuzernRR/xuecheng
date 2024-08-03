package com.lxz.base.exception;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/3 下午4:44
 */

public class XueChengPlusException extends RuntimeException {
    private String errMessage;

    public XueChengPlusException() {
    }
    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(String message){
        throw new XueChengPlusException(message);
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
