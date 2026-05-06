package com.usst.thumbs.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ThumbTypeEnum {
    INCR(1),
    DECR(-1),
    NON(0);

    private final int value;


}
