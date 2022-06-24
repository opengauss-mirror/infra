package com.opengauss.service.impl;

import com.opengauss.constant.TypeConstants;
import com.opengauss.entity.Article;
import com.opengauss.exception.ServiceException;
import com.opengauss.repository.ArticleRepository;
import com.opengauss.repository.TipsRepository;
import com.opengauss.service.SearchService;
import com.opengauss.utils.IdUtil;
import com.opengauss.utils.ParseHtmlUtil;
import com.opengauss.vo.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
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
import java.nio.charset.StandardCharsets;
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
    public void refreshDoc() {

        File indexFile = new File(basePath);
        if (!indexFile.exists()) {
            log.error(String.format("%s 文件夹不存在", indexFile.getPath()));
            log.error("服务器开小差了");
            return;
        }
        log.info("开始更新es文档");
        articleRepository.deleteAll();
        log.info("删除原数据成功");
        String lang = "";
        String type = "";
        File[] typeDir;
        File[] languageDir = indexFile.listFiles();
        for (File languageFile : languageDir) {

            lang = languageFile.getName();

            typeDir = languageFile.listFiles();

            for (File typeFile : typeDir) {
                List<Article> list = new ArrayList<>();
                type = typeFile.getName();

//                if (!TypeConstants.NEWS.equals(type)) {
//                    continue;
//                }


                Collection<File> listFiles = FileUtils.listFiles(typeFile, new String[]{"md"}, true);

                for (File mdFile : listFiles) {
                    if (!mdFile.getName().startsWith("_")) {
                        try {
                            Article a = parseMD(lang, type, mdFile);
                            if (a != null){
                                list.add(a);
                            }
                        } catch (Exception e) {
                            log.info(mdFile.getPath());
                            log.error(e.getMessage());
                        }
                    }
                }

                articleRepository.saveAll(list);
            }
        }
        log.info("更新数据成功");

    }


    public Article parseMD(String lang, String type, File mdFile) {


        Article data = new Article();


        String path = mdFile.getPath()
                .replace("\\", "/")
                .replace(basePath + lang + "/", "")
                .replace("\\\\", "/")
                .replace(".md", "");

        if (TypeConstants.DOCS.equals(type)) {
            path = path.replaceFirst(type + "/", "");
        }

        if ("post".equals(type)) {
            type = TypeConstants.BLOGS;
            path += "/";
        }

        if (path.contains("Developerguide") && path.contains("GAUSS-") && path.contains("----")) {
            path = path.replaceAll("----", "-");
        }
        data.setLang(lang);
        data.setType(type);

        data.setId(IdUtil.getId());

        data.setArticleName(path);
        data.setPath(path);

        try {
            String[] result = ParseHtmlUtil.parseHtml(FileUtils.readFileToString(mdFile, StandardCharsets.UTF_8), type);
            data.setTextContent(result[0]);
            data.setTitle(result[2]);
        } catch (IOException e) {
            throw new ServiceException("服务器开小差了");
        }

        if (TypeConstants.DOCS.equals(type)) {
            String version = path.substring(0, path.indexOf("/"));
            data.setVersion(version);
        }

        return data;
    }

    @Override
    public Map<String, Object> searchByCondition(SearchCondition condition) throws IOException {

        int startIndex = (condition.getPage() - 1) * condition.getPageSize();
        SearchRequest request = new SearchRequest("gauss_articles");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder mustBuilder = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(condition.getLang())) {
            mustBuilder.filter(QueryBuilders.termQuery("lang", condition.getLang()));
        }
        if (!StringUtils.isEmpty(condition.getType())) {
            mustBuilder.filter(QueryBuilders.termQuery("type", condition.getType()));
        }

        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(condition.getKeyword());
        multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.BEST_FIELDS);
        multiMatchQueryBuilder.field("title", 10);
        multiMatchQueryBuilder.field("textContent", 1);

        mustBuilder.must(multiMatchQueryBuilder);
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
