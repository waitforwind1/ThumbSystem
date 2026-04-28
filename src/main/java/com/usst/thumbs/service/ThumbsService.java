package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Thumbs;
import jdk.dynalink.linker.support.CompositeGuardingDynamicLinker;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Service
* @createDate 2026-04-28 21:04:00
*/
public interface ThumbsService extends IService<Thumbs> {
    boolean addThumbs(Long userid,Long blogid);

    boolean rmThumbs(Long userid,Long blogid);
}
