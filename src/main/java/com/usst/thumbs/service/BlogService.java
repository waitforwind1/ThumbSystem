package com.usst.thumbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Blog;
import io.netty.util.concurrent.SingleThreadEventExecutor;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service
* @createDate 2026-04-28 20:26:51
*/
public interface BlogService extends IService<Blog> {

     Long writeBlog(String title, String content,String image);

     Blog searchBlog(Integer id);

     boolean addThumbs(Integer userid,Integer blogid);

}
