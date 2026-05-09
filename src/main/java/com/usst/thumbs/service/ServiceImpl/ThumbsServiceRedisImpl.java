package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.RedisLuaScriptConstant;
import com.usst.thumbs.common.ThumbsConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.manager.cache.CacheManager;
import com.usst.thumbs.mapper.ThumbsMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.enums.LuaStatesEnum;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static com.usst.thumbs.common.ThumbsConstant.THUMB_CONSTANT;
import static com.usst.thumbs.common.ThumbsConstant.UN_THUMB_CONSTANT;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Service实现
* @createDate 2026-04-28 21:04:00
*/

// 这个类是第二节的实现  是为了缓解数据库的写压力 是通过在写的时候建立一个以时间片为标志的临时记录，存储到redis中，同时写点赞和取消点赞的脚本，定期执行同步到数据库
@Service("thumbsServiceRedis")
public class ThumbsServiceRedisImpl extends ServiceImpl<ThumbsMapper, Thumbs>
    implements ThumbsService{

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private BlogService blogService;

    private final CacheManager cacheManager;

    @Autowired
    private  RedisTemplate<String,Object> redisTemplate;

    public ThumbsServiceRedisImpl(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    public Boolean doThumb(DoThumbRequest doThumbRequest,HttpServletRequest request){
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
        String timeslice = getTimeSlice();
        String tempThumbKey = RedisKeyUtil.getTempThumbsKey(timeslice);
        String userThumbKey = RedisKeyUtil.getUserThumbsKey(userid);
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                // 传入脚本的集合作为KEYS，后边的参数按照顺序一一传递
                Arrays.asList(tempThumbKey,userThumbKey),
                userid,
                blogid
        );
        if(result== LuaStatesEnum.FAIL.getValue())
            throw new BusinessException(ResultType.NO_AUTH, "用户已点赞，无法重复");
        Boolean res = result==LuaStatesEnum.SUCCESS.getValue();
        if(res) cacheManager.putIfPresent(ThumbsConstant.USER_THUMB_KEY_PREFIX+userid,blogid
                .toString(),THUMB_CONSTANT);
        return res;
    }

    public Boolean undoThumb(DoThumbRequest doThumbRequest,HttpServletRequest request){
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
        String timeslice = getTimeSlice();
        String tempThumbKey = RedisKeyUtil.getTempThumbsKey(timeslice);
        String userThumbKey = RedisKeyUtil.getUserThumbsKey(userid);
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.CANCLE_THUMB_SCRIPT,
                // 传入脚本的集合作为KEYS，后边的参数按照顺序一一传递
                Arrays.asList(tempThumbKey,userThumbKey),
                userid,
                blogid
        );
        if(result== LuaStatesEnum.FAIL.getValue())
            throw new BusinessException(ResultType.NO_AUTH, "用户未点赞");
        Boolean res = result==LuaStatesEnum.SUCCESS.getValue();
        if(res) cacheManager.putIfPresent(ThumbsConstant.USER_THUMB_KEY_PREFIX+userid,blogid
                .toString(),UN_THUMB_CONSTANT);
        return res;
    }


    public String getTimeSlice() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        int second = (now.getSecond() / 10) * 10;
        return now.format(DateTimeFormatter.ofPattern("HH:mm:")) + String.format("%02d", second);
    }

    @Override
    public Boolean addThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public Boolean rmThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public Boolean hasThumb(Long userid, Long blogid) {
        if(userid==null || blogid==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"用户未登录");
//        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbsKey(userid),blogid.toString());
        Object thumbId = cacheManager.get(RedisKeyUtil.getUserThumbsKey(userid), blogid.toString());
        if(thumbId==null)
            return false;
        Number id = (Number) thumbId;
        return id.longValue() != UN_THUMB_CONSTANT;
    }
}




