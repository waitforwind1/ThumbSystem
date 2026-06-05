package com.usst.thumbs.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {
    private Long id;
    private String newUserName;
    private String newAvatar;
}
