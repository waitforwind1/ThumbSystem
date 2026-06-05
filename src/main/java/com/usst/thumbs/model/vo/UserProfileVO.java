package com.usst.thumbs.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class UserProfileVO implements Serializable {
    private Long id;
    private String userName;
    private String account;
    private String avatar;
    private Long blogCount;
    private Long thumbCount;
    private Long favoriteCount;
    private Date createTime;

}
