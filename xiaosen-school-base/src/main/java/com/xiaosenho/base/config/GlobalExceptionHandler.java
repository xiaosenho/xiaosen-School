package com.xiaosenho.base.config;

import com.xiaosenho.base.exception.CommonError;
import com.xiaosenho.base.exception.RestResponse;
import com.xiaosenho.base.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;


/**
 * @author: 作者
 * @create: 2025-02-17 12:58
 * @Description:
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//响应状态码5**
    public RestResponse customException(ServiceException e){
        log.error("【系统异常】{}",e.getErrMessage(),e);
        return new RestResponse(e.getErrMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResponse exception(Exception e) {
        log.error("【系统异常】{}",e.getMessage(),e);
        return new RestResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)//JSR303参数校验异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResponse exception(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().stream().forEach(item ->{
            errors.add(item.getDefaultMessage());
        });
        String errMsg = StringUtils.join(errors,",");
        log.error("【系统异常】{}",errMsg,e);
        return new RestResponse(errMsg);
    }
}
