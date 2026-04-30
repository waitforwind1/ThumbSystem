package com.usst.thumbs.model.vo;

import lombok.Data;

import java.security.PrivateKey;
import java.util.Date;

@Data
public class BlogVO {
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private Date createTime;
    private Integer thumbCount;
    private Boolean hasThumb;
}
