package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Share;
import com.usst.thumbs.model.request.ShareRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author 22097
* @description 针对表【share(保存分享内容数据)】的数据库操作Service
* @createDate 2026-05-16 18:18:38
*/
@Service
public interface ShareService extends IService<Share> {
    Boolean shareBlog(ShareRequest request, HttpServletRequest httpRequest);

}
