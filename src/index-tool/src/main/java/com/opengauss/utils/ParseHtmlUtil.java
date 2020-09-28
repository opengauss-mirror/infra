package com.opengauss.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.pegdown.PegDownProcessor;

public class ParseHtmlUtil {
    public static String parseHtml(String mdStr) {
        PegDownProcessor pdp = new PegDownProcessor(Integer.MAX_VALUE);
        // 将markdown转成html
        String htmlContent = pdp.markdownToHtml(mdStr);
        //去除html标签
        htmlContent = Jsoup.clean(htmlContent, Whitelist.none());
        //去除+++
        htmlContent = htmlContent.replace("+++", "");
        //定义空格,回车,换行符,制表符
//        String spaceRegex = "\\s*|\t|\r|\n";
//        htmlContent = htmlContent.replaceAll(spaceRegex, " ");
        return htmlContent.trim();
    }
}
