package com.opengauss.repository;

import com.opengauss.entity.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends ElasticsearchRepository<Article, String> {
    /**
     * 根据版本删除数据
     *
     * @param type 文档类型
     * @return 删除条数
     */
    int deleteByType(String type);

    /**
     * 根据类型和版本删除数据
     *
     * @param type    类型
     * @param version 版本
     * @return 删除条数
     */
    int deleteByTypeAndVersion(String type, String version);
}
