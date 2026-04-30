package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.ResultType;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.DoThumbRequest;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.mapper.ThumbsMapper;
import com.usst.thumbs.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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

    @Resource
    @Lazy
    private BlogService blogService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public Boolean addThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest==null || request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数为空");
        User loginUser = userService.getLoginUser(request);
        if(loginUser==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"用户未登录");
        Long userid = loginUser.getId();
        Long blogid = doThumbRequest.getBlogid();

        Blog blog = blogService.getById(blogid);
        if(blog==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"1111文章不存在");
        // 加锁
        synchronized (userid.toString().intern()){
            // 事务
            return Boolean.TRUE.equals(transactionTemplate.execute(status -> {

                Boolean exit = this.lambdaQuery()
                        .eq(Thumbs::getUserId, userid)
                        .eq(Thumbs::getBlogId, blogid)
                        .exists();
                if (exit)
                    throw new BusinessException(ResultType.NO_AUTH, "已点赞，无法重复");
                Blog newblog = blogService.searchBlog(blogid);
                Integer thumbcount = newblog.getThumbCount();
                if (thumbcount < 0)
                    throw new BusinessException(ResultType.PARAM_ERROR, "点赞数目异常");
                Boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId,blogid)
                        .setSql("thumb_count = thumb_count+1")
                        .update();
                Thumbs thumbs = new Thumbs();
                thumbs.setUserId(userid);
                thumbs.setBlogId(blogid);
                return update && this.save(thumbs);

            }));
        }

    }

    @Override
    public Boolean rmThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest==null || request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数为空");
        //需要先判断文章是否存在 用户是否存在   然后在看该用户是否为该文章点过赞 有的话在判断总的点赞数是否>0 不大于就不能再减
        User loginUser = userService.getLoginUser(request);
        if(loginUser==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"用户未登录");

        Long userid = loginUser.getId();
        Long blogid = doThumbRequest.getBlogid();

        Blog blog = blogService.getById(blogid);
        if(blog==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"文章不存在");
        // 锁+事务
        synchronized (userid.toString().intern()){

            return transactionTemplate.execute(status -> {
               Boolean exits = this.lambdaQuery()
                       .eq(Thumbs::getBlogId,blogid)
                       .eq(Thumbs::getUserId,userid)
                       .exists();
               if(!exits)
                   throw new BusinessException(ResultType.NOT_FOUND,"用户未点赞，无法取消");
               Boolean update = blogService.lambdaUpdate()
                       .eq(Blog::getId,blogid)
                       .gt(Blog::getThumbCount,0)
                       .setSql("thumb_count = thumb_count-1")
                       .update();
               boolean rm = this.lambdaUpdate()
                       .eq(Thumbs::getUserId,userid)
                       .eq(Thumbs::getBlogId,blogid)
                       .remove();
               return update && rm;
            });
        }
    }
}




