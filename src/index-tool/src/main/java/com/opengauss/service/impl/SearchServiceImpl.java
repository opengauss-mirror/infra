package com.opengauss.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.opengauss.constant.TypeConstants;
import com.opengauss.entity.Article;
import com.opengauss.repository.ArticleRepository;
import com.opengauss.repository.TipsRepository;
import com.opengauss.service.SearchService;
import com.opengauss.utils.DataFromFileUtil;
import com.opengauss.vo.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TipsRepository tipsRepository;

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient restHighLevelClient;

    @Value("${service.path}")
    private String basePath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshDoc(String type) {
        log.info(String.format("===============开始解析%s代码=============", type));
        JSONArray array = new JSONArray();
        if (TypeConstants.DOCS.equals(type)) {
            File file = new File(basePath + type);
            for (File versionFile : Objects.requireNonNull(file.listFiles())) {
                array.addAll(DataFromFileUtil.readFromFile(basePath, versionFile.getName(), type));
            }
        } else {
            array = DataFromFileUtil.readFromFile(basePath, "", type);
        }
        log.info(String.format("===============解析%s代码成功，开始更新es=============", type));
        if (array.size() > 0) {
            List<Article> list = array.toJavaList(Article.class);
            articleRepository.deleteByType(type);
            articleRepository.saveAll(list);
        }
        log.info(String.format("===============更新%s数据成功=============", type));
    }

    @Override
    public Map<String, Object> searchByCondition(SearchCondition condition) throws IOException {
        int startIndex = (condition.getPage() - 1) * condition.getPageSize();
        SearchRequest request = new SearchRequest("gauss_articles");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder mustBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder shouldBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(condition.getLang())) {
            mustBuilder.must(QueryBuilders.termQuery("lang", condition.getLang()));
        }
        if (!StringUtils.isEmpty(condition.getType())) {
            mustBuilder.must(QueryBuilders.termQuery("type", condition.getType()));
        }
        shouldBuilder.should(QueryBuilders.matchPhraseQuery("textContent", condition.getKeyword()));
        shouldBuilder.should(QueryBuilders.matchPhraseQuery("title", condition.getKeyword()));
        mustBuilder.must(shouldBuilder);
        sourceBuilder.query(mustBuilder);
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("textContent")
                .field("title")
                .fragmentSize(100)
                .requireFieldMatch(false)
                .preTags("<span>")
                .postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        sourceBuilder.from(startIndex).size(condition.getPageSize());
        sourceBuilder.timeout(TimeValue.timeValueMinutes(1L));
        sourceBuilder.aggregation(AggregationBuilders.terms("data").field("type"));
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<Article> data = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            String highLight = map.get("textContent").toString();
            String title = map.getOrDefault("title", "").toString();
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("textContent")) {
                highLight = highlightFields.get("textContent").getFragments()[0].toString();
            }
            if (highlightFields.containsKey("title")) {
                title = highlightFields.get("title").getFragments()[0].toString();
            }
            Article article = new Article().setId(hit.getId())
                    .setArticleName(map.get("articleName").toString())
                    .setLang(map.getOrDefault("lang", "").toString())
                    .setPath(map.getOrDefault("path", "").toString())
                    .setTitle(title)
                    .setVersion(map.getOrDefault("version", "").toString())
                    .setType(map.getOrDefault("type", "").toString())
                    .setTextContent(highLight);
            data.add(article);
        }
        if (data.isEmpty()) {
            return null;
        }
        // 搜索词联想
        /*List<Tips> list = tipsRepository.findByLangAndTextTip(condition.getLang(), condition.getKeyword());
        if (list.isEmpty()) {
            tipsRepository.save(new Tips(IdUtil.getId(), condition.getLang(), condition.getKeyword()));
        }*/
        List<Map<String, Object>> numberList = new ArrayList<>();
        Map<String, Object> numberMap = new HashMap<>();
        numberMap.put("count", response.getHits().getTotalHits().value);
        numberMap.put("key", "all");
        numberList.add(numberMap);
        ParsedTerms aggregation = response.getAggregations().get("data");
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Map<String, Object> countMap = new HashMap<>();
            countMap.put("key", bucket.getKeyAsString());
            countMap.put("count", bucket.getDocCount());
            numberList.add(countMap);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("page", condition.getPage());
        result.put("pageSize", condition.getPageSize());
        result.put("records", data);
        result.put("totalNum", numberList);
        return result;
    }

    @Override
    public Set<String> searchTips(String lang, String keywords) {
        try {
            SearchRequest searchRequest = new SearchRequest("gauss_tips");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.fetchSource("textTip", "");
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery("lang", lang));
            boolQueryBuilder.must(QueryBuilders.matchQuery("textTip", keywords));
            sourceBuilder.query(boolQueryBuilder);
            searchRequest.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            Set<String> result = new HashSet<>(hits.length);
            for (SearchHit hit : hits) {
                result.add(hit.getSourceAsMap().getOrDefault("textTip", "").toString());
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return new HashSet<>();
    }
}
