package com.usst.thumbs.Repository;

import com.usst.thumbs.model.es.BlogEsDoc;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface BlogRepository extends ElasticsearchRepository<BlogEsDoc,Long> {

    @Highlight(fields = {
            @HighlightField(name = "title"),
            @HighlightField(name = "summary")
    })
    List<BlogEsDoc> findByUserId(Long userId);

    List<BlogEsDoc> findByUserIdAndSummary(String summary);

}
