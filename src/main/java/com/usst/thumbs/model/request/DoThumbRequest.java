package com.usst.thumbs.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class DoThumbRequest implements Serializable {
    private Long blogid;
}
