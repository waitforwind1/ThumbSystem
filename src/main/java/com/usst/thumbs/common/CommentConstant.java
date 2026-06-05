package com.usst.thumbs.common;

public interface CommentConstant {

    String BLOG_COMMENT_FIRST_PAGE_KEY = "blog_comment_first_page:%s";

    int DEFAULT_PAGE_NO = 1;
    int DEFAULT_PAGE_SIZE = 10;
    int MAX_PAGE_SIZE = 50;

    long COMMENT_FIRST_PAGE_TTL = 10L;

    int COMMENT_NO_DELETE = 0;
    int COMMENT_IS_DELETE = 1;

    String BLOG_COMMENT_COUNT_KEY = "blog_comment_count:%s";
    long COMMENT_COUNT_TTL = 10L;
}
