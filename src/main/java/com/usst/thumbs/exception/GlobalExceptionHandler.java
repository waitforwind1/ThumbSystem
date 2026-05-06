package com.usst.thumbs.exception;

import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result businessExceptionhandler(BusinessException e){
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }
}
