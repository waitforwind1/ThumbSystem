package com.usst.thumbs.common;

public interface BlogIndexEventConstant {

    String SAVE_ACTION = "save";
    String DELETE_ACTION = "delete";

    int STATUS_WAIT_SEND = 0;
    int STATUS_SENDING = 1;
    int STATUS_SENT = 2;
    int STATUS_RETRY =3;
    int STATUS_DEAD = 4;

    int MAX_RETRY_COUNT = 3;
}
