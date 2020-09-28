package com.opengauss.utils;

import com.alibaba.fastjson.JSONArray;
import com.opengauss.constant.TypeConstants;
import com.opengauss.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DataFromFileUtil {

    public static JSONArray readFromFile(String basePath, String version, String type) {
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
                    docFile = files[0];
                } else {
                    continue;
                }
            }
            Collection<File> listFiles = FileUtils.listFiles(docFile, new String[]{"md"}, true);
            for (File mdFile : listFiles) {
                if (!mdFile.getName().startsWith("_")) {
                    jsonArray.add(DataToMap(basePath, version, type, lang, mdFile));
                }
            }
        }
        return jsonArray;
    }

    private static Map<String, String> DataToMap(String basePath, String version, String type, String lang, File mdFile) {
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
            log.error(String.format("解析文档失败,文件路径：%s", mdFile.getPath()));
            throw new ServiceException("服务器开小差了");
        }
        data.put("title", mdFile.getName().replaceAll(".md", ""));
        data.put("type", type);
        data.put("lang", lang);
        data.put("version", version);
        return data;
    }
}
