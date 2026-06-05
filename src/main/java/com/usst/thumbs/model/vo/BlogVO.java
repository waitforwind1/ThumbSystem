package com.usst.thumbs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlogVO {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;

    private String title;
    private String content;
    private String coverImage;
    private String category;
    private String tag;
    private String summary;


    private Long thumbCount;
    private Long viewCount;
    private Long favoriteCount;
    private Long commentCount;
    private Long shareCount;
    private Double hotScore;
    private Integer status;

    private Boolean hasThumb;
    private Boolean hasFavorite;
    private Boolean hasShare;

    private Date createTime;
    private Date updateTime;
}
