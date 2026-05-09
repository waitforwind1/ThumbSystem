package com.usst.thumbs.listener.thumb.msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThumbEvent implements Serializable {

    private Long userId;
    private Long blogId;
    private EventType eventType;
    private LocalDateTime eventTime;

    public enum EventType{
        INCR,
        DECR
    }
}
