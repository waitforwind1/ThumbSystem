package com.usst.thumbs.exception;

import com.usst.thumbs.result.ResultType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BusinessException extends RuntimeException{

    private final int code;
    private final String description;

    public BusinessException(ResultType resultType,String description){
        super(resultType.getMsg());
        this.code = resultType.getCode();
        this.description = description;
    }

    public BusinessException(int code,String msg,String description){
        super(msg);
        this.code=code;
        this.description=description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }
}
