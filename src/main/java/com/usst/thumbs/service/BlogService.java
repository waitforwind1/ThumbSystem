package com.usst.thumbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.request.BlogSearchRequest;
import com.usst.thumbs.model.vo.BlogInteractionVO;
import com.usst.thumbs.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service
* @createDate 2026-04-28 20:26:51
*/
@Service
public interface BlogService extends IService<Blog> {

     Boolean writeBlog(BlogAddRequest blogAddRequest,HttpServletRequest request);
     Boolean updateBlog(Long blogId, BlogAddRequest blogAddRequest, HttpServletRequest request);
     Boolean updateBlogStatus(Long blogId, Integer status, HttpServletRequest request);
     List<BlogVO> pageGetBlog(Integer pageNo,Integer pageSize,HttpServletRequest request);
     Boolean deleteBlog(Long blogId,HttpServletRequest request);
     BlogVO convertToBlogVO(Blog blog,HttpServletRequest request);

     List<BlogVO> searchBlog(BlogSearchRequest searchRequest, HttpServletRequest httpRequest);
     BlogVO blogDetail(Long blogId,HttpServletRequest httpServletRequest);
     List<BlogVO> listByAuthor(Long userId, Integer pageNo, Integer pageSize, HttpServletRequest request);
     BlogInteractionVO getInteractionStatus(Long blogId, HttpServletRequest request);

}
