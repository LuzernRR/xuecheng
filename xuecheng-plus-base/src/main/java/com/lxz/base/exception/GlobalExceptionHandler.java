package com.lxz.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/3 下午4:49
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 对项目的自定义异常
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e){
        // 记录异常
        log.error("系统异常:{}",e.getMessage());

        // 解析出异常信息
        String errMessage = e.getErrMessage();
        RestErrorResponse restErrorResponse = new RestErrorResponse(errMessage);
        return restErrorResponse;
    }
    // 对项目的其他异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e){
        // 记录异常
        log.error("系统异常:{}",e.getMessage(), e);
        if (e.getMessage().equals("不允许访问")){
            XueChengPlusException.cast("您没有操作权限！");
            return new RestErrorResponse("您没有操作权限！");
        }
        // 解析出异常信息
        RestErrorResponse restErrorResponse = new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
        return restErrorResponse;
    }

    // 解析Jsr303校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException (MethodArgumentNotValidException e){

        BindingResult bindingResult = e.getBindingResult();
        // 存储错误信息
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().stream().forEach(item -> {
            errors.add(item.getDefaultMessage());
        });
        // 将list中的错误信息用逗号拼接
        String errMessage = StringUtils.join(errors, ",");
        // 记录异常
        log.error("系统异常:{}", e.getMessage(), errMessage);

        // 解析出异常信息
        RestErrorResponse restErrorResponse = new RestErrorResponse(errMessage);
        return restErrorResponse;
}
}