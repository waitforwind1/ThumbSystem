package com.usst.thumbs.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class BlogSearchRequest implements Serializable {
    private String keyWord;
    private String tag;
    private String category;
    private Long userId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;

}
