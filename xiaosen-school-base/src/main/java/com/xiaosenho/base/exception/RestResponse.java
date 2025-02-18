package com.xiaosenho.base.exception;

import java.io.Serializable;

/**
 * @author: 作者
 * @create: 2025-02-17 12:48
 * @Description: 和前端约定返回的异常信息
 */
public class RestResponse implements Serializable {
    private String errMessage;

    public RestResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
