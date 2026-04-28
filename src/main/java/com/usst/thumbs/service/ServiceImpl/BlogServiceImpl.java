package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.ResultType;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.mapper.BlogMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2026-04-28 20:26:51
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{


    @Override
    public Long writeBlog(String title, String content, String image) {
        if(this.getOne(new QueryWrapper<Blog>()
                .eq("title",title))!=null)
            throw new BusinessException(ResultType.PARAM_ERROR,"文章已存在");
        Blog blog =new Blog();
        blog.setTitle(title);
        blog.setCoverimage(image);
        blog.setContent(content);
        this.save(blog);
        return blog.getId();
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




}




