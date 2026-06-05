package com.usst.thumbs.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class DoFavoriteRequest implements Serializable {
    private Long blogId;
}
