package com.usst.thumbs.model.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "blog_article", createIndex = false)
public class BlogEsDoc {
    @Id
    private Long id;
    @Field(type = FieldType.Keyword)
    private Long userId;
    // Docker 中使用官方 Elasticsearch 镜像，未额外安装 IK 插件；使用内置 CJK 分词器。
    @Field(type = FieldType.Text, analyzer = "cjk", searchAnalyzer = "cjk")
    private String content;
    @Field(type = FieldType.Text, analyzer = "cjk", searchAnalyzer = "cjk")
    private String title;
    @Field(type = FieldType.Text, analyzer = "cjk", searchAnalyzer = "cjk")
    private String summary;
    @Field(type = FieldType.Keyword)
    private String category;
    @Field(type = FieldType.Keyword)
    private String tag;

    @Field(type = FieldType.Integer)
    private Integer status;
}
