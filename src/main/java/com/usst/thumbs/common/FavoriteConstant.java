package com.usst.thumbs.common;

public interface FavoriteConstant {

    String USER_FAVORITE_KEY = "user:favorite:%s";

    /** Marks that the user's historical favorite records have been loaded into Redis. */
    String USER_FAVORITE_STATE_READY_KEY = "user:favorite:state:ready:%s";

    String BLOG_FAVORITE_COUNT_KEY = "blog:favorite:count:key:%s";

    int ACTION_ADD = 1;
    int ACTION_CANCEL = 0;

    double HOT_SCORE_FAVORITE =3.0;

}
