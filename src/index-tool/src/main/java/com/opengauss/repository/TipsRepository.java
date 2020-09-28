package com.opengauss.repository;

import com.opengauss.entity.Tips;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipsRepository extends ElasticsearchRepository<Tips, String> {
    /**
     * 根据语言和搜索词查询
     *
     * @param lang 语言
     * @param tip  搜索词
     * @return 是否存在
     */
    List<Tips> findByLangAndTextTip(String lang, String tip);
}
