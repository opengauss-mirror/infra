package com.opengauss.controller;

import com.opengauss.constant.TypeConstants;
import com.opengauss.service.SearchService;
import com.opengauss.vo.SearchCondition;
import com.opengauss.vo.SysResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
public class SearchController {
    @Autowired
    private SearchService searchService;

    @Value("${update.shell}")
    private String updateShellPath;

    /**
     * 搜索关键词联想
     *
     * @param keywords 关键字
     * @param lang     语言
     * @return 搜索词
     */
    @GetMapping("tips")
    public SysResult tipSearch(@RequestParam(value = "keywords") String keywords,
                               @RequestParam(value = "lang") String lang) {
        keywords = keywords.replaceAll("#", "");
        Set<String> set = searchService.searchTips(lang, keywords);
        return SysResult.ok("联想成功", set);
    }

    /**
     * 查询文档
     *
     * @param condition 封装查询条件
     * @return 搜索结果
     */
    @PostMapping("docs")
    public SysResult searchDocByKeyword(@RequestBody SearchCondition condition) {
        if (!StringUtils.hasText(condition.getKeyword())) {
            return SysResult.fail("keyword must not null", null);
        }
        condition.setKeyword(condition.getKeyword().replace("#", ""));
        try {
            Map<String, Object> result = searchService.searchByCondition(condition);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return SysResult.fail("查询失败", null);
    }

    /**
     * 用于测试接口
     *
     * @return 返回测试
     */
    @GetMapping("index")
    public SysResult index() {
        return SysResult.ok("访问成功", null);
    }

    /**
     * 定时任务
     */
    @Scheduled(cron = "${scheduled.cron}")
    public String scheduledTask() {
        BeginFun.lock.lock();
        Process process;
        try {
            log.info("===============开始拉取仓库资源=================");
            process = Runtime.getRuntime().exec(updateShellPath);
            process.waitFor();
            List<String> result = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);
            log.info(result.toString());
            log.info("===============仓库资源拉取成功=================");
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }

        searchService.refreshDoc();
        BeginFun.lock.unlock();
        return "success";
    }

}
