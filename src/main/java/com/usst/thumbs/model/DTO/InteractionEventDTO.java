package com.usst.thumbs.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionEventDTO implements Serializable {
    private Long id;
    private String eventId;
    private Long userId;
    private Long  blogId;
    private Long targetUserId;
    private Long commentId;
    private Integer type;
    private Integer action;
}
