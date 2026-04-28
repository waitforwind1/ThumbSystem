package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.ResultType;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.User;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.mapper.ThumbsMapper;
import com.usst.thumbs.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Service实现
* @createDate 2026-04-28 21:04:00
*/
@Service
public class ThumbsServiceImpl extends ServiceImpl<ThumbsMapper, Thumbs>
    implements ThumbsService{

    @Resource
    private UserService userService;
    @Autowired
    private BlogService blogService;

    @Override
    public boolean addThumbs(Long userid, Long blogid) {
        if(userid==null || blogid==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"id为空");
        User user = userService.getById(userid);
        if(user==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"用户不存在");
        Blog blog = blogService.getById(blogid);
        if(blog==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"文章不存在");
        Thumbs res = this.getOne(new QueryWrapper<Thumbs>()
                .eq("userid",userid)
                .eq("blogid",blogid));
        if(res!=null)
            throw new BusinessException(ResultType.NO_AUTH,"已点赞，无法重复");
        Blog newblog= blogService.searchBlog(blogid);
        Integer thumbcount = newblog.getThumbcount();
        if(thumbcount<0)
            throw new BusinessException(ResultType.PARAM_ERROR,"点赞数目异常");
        newblog.setThumbcount(thumbcount+1);
        boolean b = blogService.updateById(newblog);
        if(b==false)
            throw new BusinessException(ResultType.DATABASE_ERROR,"点赞失败");
        Thumbs thumbs = new Thumbs();
        thumbs.setUserid(userid);
        thumbs.setBlogid(blogid);
        return this.save(thumbs);
    }

    @Override
    public boolean rmThumbs(Long userid, Long blogid) {
        //需要先判断文章是否存在 用户是否存在   然后在看该用户是否为该文章点过赞 有的话在判断总的点赞数是否>0 不大于就不能再减
        if(userid==null || blogid==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"id为空");
        User user = userService.getById(userid);
        if(user==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"用户不存在");
        Blog blog = blogService.getById(blogid);
        if(blog==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"文章不存在");
        Thumbs res = this.getOne(new QueryWrapper<Thumbs>()
                .eq("userid",userid)
                .eq("blogid",blogid));
        if(res==null)
            throw new BusinessException(ResultType.NO_AUTH,"未点赞，无法取消");
        Blog blog1= blogService.searchBlog(blogid);
        Integer thumbcount = blog.getThumbcount();
        if(thumbcount<=0)
            throw new BusinessException(ResultType.PARAM_ERROR,"点赞数目异常");
        blog1.setThumbcount(thumbcount-1);
        boolean b = blogService.updateById(blog1);
        if(b==false)
            throw new BusinessException(ResultType.DATABASE_ERROR,"点赞失败");
        Thumbs thumbs = new Thumbs();
        thumbs.setUserid(userid);
        thumbs.setBlogid(blogid);
        return this.removeById(thumbs);
    }
}




