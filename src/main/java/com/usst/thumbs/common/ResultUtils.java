package com.usst.thumbs.common;

public class ResultUtils {
    public static <T> Result<T> success(ResultType resultType,T data,String description){
        return new Result<>(resultType.getCode(),resultType.getMsg(),data,description);
    }

    public static <T> Result<T> error(ResultType resultType,T data,String description){
        return new Result<>(resultType.getCode(),resultType.getMsg(),data,description);
    }

    public static <T> Result<T> error(ResultType resultType,String description){
        return new Result<>(resultType.getCode(),resultType.getMsg(),null,description);
    }

    public static <T> Result<T> error(int code,String msg,T data,String description){
        return new Result<>(code,msg,data,description);
    }
    public static <T> Result<T> error(int code,String msg,String description){
        return new Result<>(code,msg,null,description);
    }
}
