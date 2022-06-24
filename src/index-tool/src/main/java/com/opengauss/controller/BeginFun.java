package com.opengauss.controller;



import com.opengauss.constant.TypeConstants;
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
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class BeginFun implements ApplicationRunner {

    @Autowired
    public SearchService searchService;

    @Value("${update.shell}")
    private String updateShellPath;



    //定义Lock锁
    public static ReentrantLock lock = new ReentrantLock();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        lock.lock();
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
        lock.unlock();
    }
}
