package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.ThumbsConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.manager.cache.CacheManager;
import com.usst.thumbs.mapper.ThumbsMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.DoThumbRequest;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.service.UserService;
import com.usst.thumbs.utils.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import static com.usst.thumbs.common.ThumbsConstant.UN_THUMB_CONSTANT;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Service实现
* @createDate 2026-04-28 21:04:00
*/
@Service("ThumbsServiceDB")
public class ThumbsServiceImpl extends ServiceImpl<ThumbsMapper, Thumbs>
    implements ThumbsService{

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private BlogService blogService;

    private final CacheManager cacheManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private  RedisTemplate<String,Object> redisTemplate;

    public ThumbsServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

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

                Boolean exit = this.hasThumb(userid,blogid);
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
                boolean success = update && this.save(thumbs);
                if(success){
                    this.redisTemplate.opsForHash().put(RedisKeyUtil.getUserThumbsKey(userid),blogid.toString(),thumbs.getId());
                    cacheManager.putIfPresent(ThumbsConstant.USER_THUMB_KEY_PREFIX+userid,blogid.toString(),thumbs.getId());
                }
                return success;
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
                // 从redis中获取是否有记录--》有，更新数据库 并删除redis中缓存记录
                Object thumbsObj = hasThumb(userid,blogid);
               if(thumbsObj==null)
                   throw new BusinessException(ResultType.NOT_FOUND,"用户未点赞，无法取消");
               Boolean update = blogService.lambdaUpdate()
                       .eq(Blog::getId,blogid)
                       .gt(Blog::getThumbCount,0)
                       .setSql("thumb_count = thumb_count-1")
                       .update();
               Long thumbId = Long.valueOf(thumbsObj.toString());
               boolean rm = this.removeById(thumbId);
               boolean success = update && rm;
               if(success){
                   redisTemplate.opsForHash().delete(RedisKeyUtil.getUserThumbsKey(userid),blogid.toString());
                   cacheManager.putIfPresent(ThumbsConstant.USER_THUMB_KEY_PREFIX+userid,blogid.toString(),UN_THUMB_CONSTANT);
               }
               return success;
            });
        }
    }

    @Override
    public Boolean hasThumb(Long userid, Long blogid) {
        if(userid==null || blogid==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"用户未登录");
//        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbsKey(userid),blogid.toString());
        Object thumbId = cacheManager.get(ThumbsConstant.USER_THUMB_KEY_PREFIX + userid, blogid.toString());
        if(thumbId==null)
            return false;
        Long id =(Long)thumbId;
        // 这里查询是否已点赞  如果已点赞的话 这里返回的就是true，没有点赞
        return !id.equals(UN_THUMB_CONSTANT);
    }

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return null;
    }
}




