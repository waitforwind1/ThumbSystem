package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.es.BlogEsDoc;
import com.usst.thumbs.model.request.BlogSearchRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.BlogSearchService;
import com.usst.thumbs.service.BlogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BlogSearchServiceImpl implements BlogSearchService {
    @Resource
    private ElasticsearchOperations elasticsearchOperations;
    @Resource
    private BlogService blogService;

    @Override
    public Result<List<BlogVO>> search(BlogSearchRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getKeyWord() == null || request.getKeyWord().isBlank()) {
            return ResultUtils.success(List.of());
        }
        String keyWord = request.getKeyWord();
        int pageNum = request.getPageNo() == null || request.getPageNo() <= 0 ? 1 : request.getPageNo();
        int pageSize = request.getPageSize() == null || request.getPageSize() <= 0
                ? 10
                : Math.min(request.getPageSize(), 50);
        List<Query> filters = new ArrayList<>();
        filters.add(Query.of(q -> q.term(t -> t
                .field("status")
                .value(BlogConstant.BLOG_STATUS_PUBLISHED))));
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            filters.add(Query.of(q -> q.term(t -> t
                    .field("category")
                    .value(request.getCategory()))));
        }
        if (request.getTag() != null && !request.getTag().isBlank()) {
            // tag 在库中以逗号保存多个值，使用通配符保持与原数据库 like 查询一致。
            filters.add(Query.of(q -> q.wildcard(w -> w
                    .field("tag")
                    .value("*" + request.getTag() + "*"))));
        }
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .query(keyWord).fields("title", "content", "tag", "summary","category")
                        )
                )
                .withFilter(f -> f.bool(b -> b.filter(filters)))
                .withHighlightQuery(new HighlightQuery(
                        new Highlight(
                                List.of(
                                        new HighlightField("title"),
                                        new HighlightField("content"),
                                        new HighlightField("summary")
                                )
                        ),
                        BlogEsDoc.class
                ))
                .withPageable(PageRequest.of(pageNum-1,pageSize))
                .build();
        SearchHits<BlogEsDoc> searchHits = elasticsearchOperations.search(query,BlogEsDoc.class);
        if (searchHits.isEmpty()) {
            return ResultUtils.success(List.of());
        }
        // ES 负责检索、排序和高亮；完整展示字段仍以数据库为准，避免前端拿到残缺文章卡片。
        List<Long> blogIds = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .toList();
        Map<Long, Blog> blogsById = blogService.list(new LambdaQueryWrapper<Blog>()
                        .in(Blog::getId, blogIds)
                        .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED))
                .stream()
                .collect(Collectors.toMap(Blog::getId, blog -> blog));

        List<BlogVO> blogVOList = new ArrayList<>();
        for (SearchHit<BlogEsDoc> searchHit : searchHits) {
            BlogEsDoc blogEsDoc = searchHit.getContent();
            Blog blog = blogsById.get(blogEsDoc.getId());
            // 索引可能尚未消费到删除/下线事件，不能把失效文档返回给前端。
            if (blog == null) {
                continue;
            }
            BlogVO blogVO = blogService.convertToBlogVO(blog, httpRequest);
            List<String> highLightTitles = searchHit.getHighlightField("title");
            if(CollUtil.isNotEmpty(highLightTitles))
                blogVO.setHighlightTitle(highLightTitles.getFirst());
            List<String> content = searchHit.getHighlightField("content");
            if(CollUtil.isNotEmpty(content))
                blogVO.setHighlightContent(content.getFirst());
            List<String> summary = searchHit.getHighlightField("summary");
            if(CollUtil.isNotEmpty(summary))
                blogVO.setHighlightSummary(summary.getFirst());
            blogVOList.add(blogVO);
        }
        return ResultUtils.success(blogVOList);
    }
}
