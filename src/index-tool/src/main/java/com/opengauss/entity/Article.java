package com.opengauss.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * 文档
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(indexName = "chenyang_gauss_articles", shards = 5, replicas = 1)
public class Article implements Serializable {
    private static final long serialVersionUID = 5842476471171664561L;
    @Id
    private String id;
    /**
     * 文件全路径
     */
    @Field(type = FieldType.Keyword)
    private String articleName;
    /**
     * 页面访问路径
     */
    @Field(type = FieldType.Keyword)
    private String path;
    /**
     * 文件内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String textContent;
    /**
     * 文件名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    /**
     * 文件类型
     */
    @Field(type = FieldType.Keyword)
    private String type;

    /**
     * 语言
     */
    @Field(type = FieldType.Keyword)
    private String lang;

    /**
     * 版本
     */
    @Field(type = FieldType.Keyword)
    private String version;
}
