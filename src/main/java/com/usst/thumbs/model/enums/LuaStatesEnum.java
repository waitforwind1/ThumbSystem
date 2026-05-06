package com.usst.thumbs.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LuaStatesEnum {
    SUCCESS(1L),
    FAIL(-1L);
    private final long value;
}
