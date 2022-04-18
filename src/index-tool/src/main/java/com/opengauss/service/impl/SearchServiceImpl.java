package com.opengauss.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.opengauss.constant.TypeConstants;
import com.opengauss.entity.Article;
import com.opengauss.exception.ServiceException;
import com.opengauss.repository.ArticleRepository;
import com.opengauss.repository.TipsRepository;
import com.opengauss.service.SearchService;
import com.opengauss.utils.DataFromFileUtil;
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
    public void refreshDoc(String type) {

        File indexFile = new File(basePath + type);
        if (!indexFile.exists()) {
            log.error(String.format("%s 文件夹不存在", indexFile.getPath()));
            log.error("服务器开小差了");
            return;
        }

        articleRepository.deleteByType(type);
        log.info("删除es - type:" + type);

        if (TypeConstants.DOCS.equals(type)) {
            File file = new File(basePath + type);
            for (File versionFile : Objects.requireNonNull(file.listFiles())) {
                readFromFile(basePath, versionFile.getName(), type);
            }
        } else {
            readFromFile(basePath, "", type);
        }

        log.info("更新数据成功:" + type);
    }


    public void readFromFile(String basePath, String version, String type) {
        File indexFile = new File(basePath + type);
        if (!indexFile.exists()) {
            log.error(String.format("%s 文件夹不存在", indexFile.getPath()));
            throw new ServiceException("服务器开小差了");
        }
        File[] languageDir;
        if (StringUtils.hasText(version)) {
            File[] versionFiles = indexFile.listFiles(file -> version.equals(file.getName()));
            if (null == versionFiles || versionFiles.length == 0) {
                log.error(String.format("%s 文件夹不存在", indexFile.getPath() + File.pathSeparator + version));
                throw new ServiceException("服务器开小差了");
            }
            File versionFile = versionFiles[0];
            languageDir = versionFile.listFiles();
        } else {
            languageDir = indexFile.listFiles();
        }
        JSONArray jsonArray = new JSONArray();
        for (File languageFile : languageDir) {
            String lang = languageFile.getName();
            File docFile;
            if (TypeConstants.BLOGS.equals(type)) {
                File[] files = languageFile.listFiles(fileName -> "post".equals(fileName.getName()));
                if (null != files && files.length == 1) {
                    docFile = files[0];
                } else {
                    continue;
                }
            } else {
                File[] files = languageFile.listFiles(fileName -> type.equals(fileName.getName()));
                if (null != files && files.length == 1) {
                    System.out.println(type + files[0]);
                    docFile = files[0];
                } else {
                    continue;
                }
            }

            Collection<File> listFiles = FileUtils.listFiles(docFile, new String[]{"md"}, true);
            for (File mdFile : listFiles) {
                if (!mdFile.getName().startsWith("_")) {
                    try {
                        jsonArray.add(DataToMap(basePath, version, type, lang, mdFile));
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }

            log.info(String.format("===============解析%s%s代码成功，开始更新es=============", type, docFile));
            List<Article> list = jsonArray.toJavaList(Article.class);
            articleRepository.saveAll(list);
            log.info(String.format("===============更新%s%s数据成功=============", type, docFile));
        }
    }





    private Map<String, String> DataToMap(String basePath, String version, String type, String lang, File mdFile) {
        Map<String, String> data = new HashMap<>();
        String articleName = mdFile.getPath().replace(basePath, "")
                .replace("\\\\", "/")
                .replace(".md", "")
                .replace("/" + lang + "/", "/");
        if (!TypeConstants.DOCS.equals(articleName)) {
            articleName = articleName.replaceFirst(type + "/", "");
        }
        data.put("id", IdUtil.getId());
        data.put("articleName", articleName);
        data.put("path", type);
        try {
            data.put("textContent", ParseHtmlUtil.parseHtml(FileUtils.readFileToString(mdFile, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new ServiceException("服务器开小差了");
        }
        data.put("title", mdFile.getName().replaceAll(".md", ""));

        data.put("type", type);

        data.put("lang", lang);

        data.put("version---", version);

        return data;
    }





    @Override
    public Map<String, Object> searchByCondition(SearchCondition condition) throws IOException {
        int startIndex = (condition.getPage() - 1) * condition.getPageSize();
        SearchRequest request = new SearchRequest("chenyang_gauss_articles");
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
            SearchRequest searchRequest = new SearchRequest("chenyang_gauss_tips");
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
