package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.usst.thumbs.common.ResultType;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2026-04-28 20:26:51
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

    @Resource
    private  UserService userService;

    @Resource
    @Lazy
    private  ThumbsService thumbsService;


    @Override
    public Boolean writeBlog(BlogAddRequest blogAddRequest,HttpServletRequest request) {
        if(this.getOne(new QueryWrapper<Blog>()
                .eq("title",blogAddRequest.getTitle()))!=null)
            throw new BusinessException(ResultType.PARAM_ERROR,"文章已存在");
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogAddRequest,blog);
        blog.setUserId(userService.getLoginUser(request).getId());
        return this.save(blog);

    }

    @Override
    public Blog searchBlog(Long id) {
        if(id==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"文章id为空");
        Blog byId = this.getById(id);
        if(byId==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"文章不存在");
        return byId;
    }

    @Override
    public BlogVO getBlogvoByid(Long blogid,HttpServletRequest request) {
        Blog blog = this.getById(blogid);
        User user = userService.getLoginUser(request);
        return getBlogvo(blog,user);
    }

    // 转换成blogVO
    public BlogVO getBlogvo(Blog blog, User user){
        BlogVO blogVO =new BlogVO();
        BeanUtils.copyProperties(blog,blogVO);
        if(user==null)
            return blogVO;
        Thumbs thumbs = thumbsService.lambdaQuery()
                .eq(Thumbs::getUserId,user.getId())
                .eq(Thumbs::getBlogId,blog.getId())
                .one();
        blogVO.setHasThumb(thumbs!=null);
        return blogVO;
    }

    @Override
    public List<BlogVO> getblogLists(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long,Boolean> thumbsHashMap =new HashMap<>();
        if(ObjectUtils.isNotEmpty(loginUser)){
            Set<Long> blogidsSet = blogList.stream().map(Blog::getId).collect(Collectors.toSet());
            // 对去重后的每篇文章获取点赞
            List<Thumbs> thumbsList = thumbsService.lambdaQuery()
                    .eq(Thumbs::getUserId,loginUser.getId())
                    .in(Thumbs::getBlogId,blogidsSet)
                    .list();
            thumbsList.forEach(thumbs -> thumbsHashMap.put(thumbs.getBlogId(),true));
        }
        return blogList.stream()
                .map(blog -> {
                    BlogVO blogVO= new BlogVO();
                    BeanUtils.copyProperties(blog,blogVO);
                    blogVO.setHasThumb(thumbsHashMap.get(blogVO.getId()));
                    return blogVO;
                }).toList();
    }


}




