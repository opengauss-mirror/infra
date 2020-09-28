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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(indexName = "gauss_tips", shards = 5, replicas = 1)
public class Tips implements Serializable {

    private static final long serialVersionUID = -5333177350140861244L;

    @Id
    private String id;

    /**
     * 语言
     */
    @Field(type = FieldType.Keyword)
    private String lang;

    /**
     * 搜索词
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String textTip;
}
