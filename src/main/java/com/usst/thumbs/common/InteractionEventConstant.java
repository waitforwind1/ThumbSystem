package com.usst.thumbs.common;

public interface InteractionEventConstant {

    int THUMB_TYPE = 1;
    int COMMENT_TYPE = 2;
    int FAVORITE_TYPE = 3;
    int REPLY_TYPE = 4;
    int SHARE_TYPE = 5;

    int ACTION_CANCEL = 0;
    int ACTION_ADD = 1;

    int STATUS_WAIT_SEND = 0;
    int STATUS_SENT = 1;
    int STATUS_CONSUMED =2;
    int STATUS_CONSUMED_FAIL = 3;
    int STATUS_SNED_FAIL = 4;

    int MAX_RETRY_COUNT = 3;
}
