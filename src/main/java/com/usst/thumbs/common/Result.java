package com.usst.thumbs.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T  data;
    private String description;

    public Result(ResultType resultType,T data,String description){
        this(resultType.getCode(),resultType.getMsg(),data,description);
    }

    // 没返回数据data的Result类型
    public Result(ResultType resultType,String description){
        this(resultType.getCode(),resultType.getMsg(),null,description);
    }

}
