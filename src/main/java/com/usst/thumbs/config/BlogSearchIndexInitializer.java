package com.usst.thumbs.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.usst.thumbs.Repository.BlogRepository;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.es.BlogEsDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.List;

/** Creates a missing article index and rebuilds it from MySQL after index recreation. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlogSearchIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;
    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            IndexOperations indexOperations = elasticsearchOperations.indexOps(BlogEsDoc.class);
            if (indexOperations.exists()) {
                return;
            }

            indexOperations.create(Document.parse("""
                    {
                      "number_of_shards": 1,
                      "number_of_replicas": 0
                    }
                    """));
            indexOperations.putMapping(Document.parse("""
                    {
                      "properties": {
                        "id": { "type": "long" },
                        "userId": { "type": "keyword" },
                        "title": { "type": "text", "analyzer": "cjk", "search_analyzer": "cjk" },
                        "content": { "type": "text", "analyzer": "cjk", "search_analyzer": "cjk" },
                        "summary": { "type": "text", "analyzer": "cjk", "search_analyzer": "cjk" },
                        "category": { "type": "keyword" },
                        "tag": { "type": "keyword" },
                        "status": { "type": "integer" }
                      }
                    }
                    """));

            List<BlogEsDoc> documents = blogMapper.selectList(new LambdaQueryWrapper<Blog>()
                            .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED))
                    .stream()
                    .map(this::toEsDoc)
                    .toList();
            if (!documents.isEmpty()) {
                blogRepository.saveAll(documents);
            }
            log.info("Created Elasticsearch index blog_article and rebuilt {} published article(s)", documents.size());
        } catch (Exception exception) {
            // ES 不可用时不阻塞主应用启动，后续重启应用即可再次触发初始化。
            log.error("Failed to initialize Elasticsearch article index", exception);
        }
    }

    private BlogEsDoc toEsDoc(Blog blog) {
        BlogEsDoc document = new BlogEsDoc();
        BeanUtils.copyProperties(blog, document);
        return document;
    }
}
