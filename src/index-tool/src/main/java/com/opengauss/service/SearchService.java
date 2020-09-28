package com.opengauss.service;

import com.opengauss.vo.SearchCondition;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface SearchService {
    /**
     * 根据索引名称和版本刷新数据
     *
     * @param type 类型
     */
    void refreshDoc(String type);

    /**
     * 根据条件搜索
     *
     * @param condition 搜索条件
     * @return 符合条件记录
     * @throws IOException
     */
    Map<String, Object> searchByCondition(SearchCondition condition) throws IOException;

    /**
     * 搜索关键词联想
     *
     * @param keywords 关键字
     * @param lang     语言
     * @return 搜索词
     */
    Set<String> searchTips(String lang, String keywords);
}
