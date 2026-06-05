package com.usst.thumbs.common;


public interface MessageConstant {
    String MESSAGE_UNREAD_KEY = "message:unread:%s";

    int TYPE_THUMB = 1;
    int TYPE_COMMENT = 2;
    int TYPE_REPLY = 3;
    int TYPE_FAVORITE = 4;
    int TYPE_SHARE = 5;
    int TYPE_SYSTEM = 6;

    int READ = 1;
    int UNREAD = 0;

    int DEFAULT_PAGE_NO = 1;
    int DEFAULT_PAGE_SIZE = 10;
    int MAX_PAGE_SIZE = 50;
}
