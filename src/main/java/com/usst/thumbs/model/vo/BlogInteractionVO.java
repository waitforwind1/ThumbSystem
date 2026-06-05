package com.usst.thumbs.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class BlogInteractionVO implements Serializable {
    private Long blogId;
    private boolean hasThumb;
    private boolean hasFavorite;
    private boolean hasShare;
    private Long thumbCount;
    private Long favoriteCount;
}
