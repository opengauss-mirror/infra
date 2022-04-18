package com.opengauss.controller;

import com.opengauss.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Component
@Slf4j
public class Sttt implements ApplicationRunner {

        private static final String[] TYPES = {"docs", "blogs", "events", "news"};
//    private static final String[] TYPES = {"blogs", "events", "news"};

//    private static final String[] TYPES = {"events"};

//    private static final String[] TYPES = {"docs"};

//    private static final String[] TYPES = {"blogs"};

//    private static final String[] TYPES = {"news"};


    @Value("${update.shell}")
    private String updateShellPath;
    @Autowired
    private SearchService searchService;


    @Override
    public void run(ApplicationArguments args) {

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

        for (String type : TYPES) {
            searchService.refreshDoc(type);
        }

    }

}