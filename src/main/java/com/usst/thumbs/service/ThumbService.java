package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Thumb;
import com.usst.thumbs.model.request.DoThumbRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Service
* @createDate 2026-04-28 21:04:00
*/
@Service
public interface ThumbService extends IService<Thumb> {

    Boolean doThumb(DoThumbRequest doThumbRequest,HttpServletRequest request);

    Boolean undoThumb(DoThumbRequest doThumbRequest,HttpServletRequest request);

    Boolean hasThumb(Long userId, Long blogId);
}
