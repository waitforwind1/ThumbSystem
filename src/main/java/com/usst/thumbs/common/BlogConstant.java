package com.usst.thumbs.common;

public interface BlogConstant {

    String BLOG_FIRST_PAGE_KEY = "blog:first:page";

    String BLOG_DETAIL_LOCAL_KEY = "blog:detail:%s";

    String BLOG_PAGE_KEY = "blog:page:%s:%s";

    // 详情页缓存过期时间设置为30min
    Long BLOG_DETAIL_TTL = 30L;

    // 单页缓存过期时间设置为5min
    Long BLOG_PAGE_TTL = 5L;

    // 博客状态
    int BLOG_STATUS_DRAFT = 0;
    int BLOG_STATUS_PUBLISHED = 1;
    int BLOG_STATUS_REVIEWING = 2;
    int BLOG_STATUS_REJECTED= 3;
    int BLOG_STATUS_OFFLINE = 4;

    // 首页查询默认值
    int DEFAULT_PAGE_NO = 1;
    int DEFAULT_PAGE_SIZE = 10;

    int DEFAULT_MAX_PAGE_SIZE = 50;
}
