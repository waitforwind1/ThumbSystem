package com.usst.thumbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.filters.ExpiresFilter;

import java.util.List;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service
* @createDate 2026-04-28 20:26:51
*/
public interface BlogService extends IService<Blog> {

     Boolean writeBlog(BlogAddRequest blogAddRequest,HttpServletRequest request);

     Blog searchBlog(Long id);

     BlogVO getBlogvoByid(Long blogid, HttpServletRequest request);

     List<BlogVO> getblogLists(List<Blog> blogList, HttpServletRequest request);


}
