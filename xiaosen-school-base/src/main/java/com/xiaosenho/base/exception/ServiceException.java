package com.xiaosenho.base.exception;

/**
 * @author: 作者
 * @create: 2025-02-17 12:51
 * @Description:
 */
public class ServiceException extends RuntimeException{

    private String errMessage;

    public ServiceException(String errMessage) {
        this.errMessage = errMessage;
    }

    public ServiceException(String message, String errMessage) {
        super(message);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(String message){
        throw new ServiceException(message);
    }

    public static void cast(CommonError error){
        throw new ServiceException(error.getErrMessage());
    }
}
