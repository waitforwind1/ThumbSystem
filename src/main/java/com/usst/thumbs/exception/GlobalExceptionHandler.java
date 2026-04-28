package com.usst.thumbs.exception;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.usst.thumbs.common.Result;
import com.usst.thumbs.common.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result businessExceptionhandler(BusinessException e){
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }
}
