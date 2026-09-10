package com.usst.thumbs.service;

import com.usst.thumbs.model.request.BlogSearchRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.Result;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface BlogSearchService {

    Result<List<BlogVO>> search(BlogSearchRequest blogSearchRequest, HttpServletRequest request);
}
