package com.usst.thumbs.service;

import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface HotService {
    void initBlogHotScore(Long blogId);
    void incrHotScore(Long blogId,double score);
    List<BlogVO> listHot(Integer limit, HttpServletRequest request);
    Double calculateHotScore(Blog blog);

    List<BlogVO> listHotByCategory(String category, Integer limit, HttpServletRequest request);
}
