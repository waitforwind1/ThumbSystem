package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Favorite;
import com.usst.thumbs.model.request.DoFavoriteRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 22097
* @description 针对表【favorite】的数据库操作Service
* @createDate 2026-05-16 18:18:35
*/
@Service
public interface FavoriteService extends IService<Favorite> {

    // todo:博客字段设计需要添加字段  比如被收藏数目  在添加收藏的时候需要同时增加被收藏数

    /**
     * 用户添加收藏 博客增长被收藏数
     * @param doFavoriteRequest
     * @return
     */
    Boolean doFavorite(DoFavoriteRequest doFavoriteRequest, HttpServletRequest request);

    /**
     * 删除收藏
     * @param doFavoriteRequest
     * @return
     */
    Boolean undoFavorite(DoFavoriteRequest doFavoriteRequest,HttpServletRequest request);

    Boolean hasFavorite(Long userId,Long blogId);

    List<Blog> listMyFavorite(HttpServletRequest request);
}
