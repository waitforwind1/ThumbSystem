package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.service.UserService;
import com.usst.thumbs.utils.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Resource(name = "thumbService")
    @Lazy
    private  ThumbsService thumbsService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


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
        Boolean thumbs = thumbsService.hasThumb(user.getId(),blog.getId());
        blogVO.setHasThumb(thumbs);
        return blogVO;
    }

    @Override
    public List<BlogVO> getblogLists(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long,Boolean> thumbsHashMap =new HashMap<>();
        if(ObjectUtils.isNotEmpty(loginUser)){
            List<Object> blogidsSet = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
            List<Object> thumbsList = redisTemplate.opsForHash().multiGet(RedisKeyUtil.getUserThumbsKey(loginUser.getId()),blogidsSet);
            for(int i=0;i<thumbsList.size();i++){
                if(thumbsList.get(i)==null)
                    continue;
                // todo:这里的和续写项目中的写的不太一样  直接强转格式有风险 这种写法更保险
                //  项目里的：put(Long.valueOf(blogIdList.get(i).toString()), true);
                thumbsHashMap.put(Long.valueOf(blogidsSet.get(i).toString()),true);
            }
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




