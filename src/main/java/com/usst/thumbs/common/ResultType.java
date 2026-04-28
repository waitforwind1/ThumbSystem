package com.usst.thumbs.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public enum ResultType {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),

    PARAM_ERROR(400, "参数错误"),
    NOT_LOGIN(401, "未登录"),
    NO_AUTH(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_EXIST(10001, "用户不存在"),
    USER_ACCOUNT_EXISTS(10002, "账号已存在"),
    USER_PASSWORD_ERROR(10003, "密码错误"),
    USER_ACCOUNT_DISABLED(10004, "账号已被禁用"),
    PARAMS_EMPTY(10005, "输入为空"),


    DATABASE_ERROR(20001, "数据库操作失败"),
    UPLOAD_ERROR(30001, "文件上传失败");

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
