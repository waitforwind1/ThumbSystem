package com.usst.thumbs.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {
    private String account;
    private String username;
    private String avatar;
    private Integer isAdmin;
    private Date createTime;
    private Date updateTime;
}
