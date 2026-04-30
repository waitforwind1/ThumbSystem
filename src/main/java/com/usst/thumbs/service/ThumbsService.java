package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.request.DoThumbRequest;
import jakarta.servlet.http.HttpServletRequest;
import jdk.dynalink.linker.support.CompositeGuardingDynamicLinker;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Service
* @createDate 2026-04-28 21:04:00
*/
public interface ThumbsService extends IService<Thumbs> {
    Boolean addThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean rmThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request);
}
