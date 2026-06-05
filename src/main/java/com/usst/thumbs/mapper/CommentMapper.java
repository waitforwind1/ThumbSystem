package com.usst.thumbs.mapper;

import com.usst.thumbs.model.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 22097
* @description 针对表【comment(保存用户评论)】的数据库操作Mapper
* @createDate 2026-05-16 18:18:31
* @Entity com.usst.thumbs.model.Comment
*/
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}




