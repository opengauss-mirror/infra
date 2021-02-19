package com.opengauss.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.pegdown.PegDownProcessor;


public class ParseHtmlUtil {
    private static final String lineRegex = "\\s*|\t|\r|\n";
    private static final String regex = "\\+\\+\\+(.*?)\\+\\+\\+";

    public static String parseHtml(String mdStr) {
        PegDownProcessor pdp = new PegDownProcessor(Integer.MAX_VALUE);
        // 将markdown转成html
        String htmlContent = pdp.markdownToHtml(mdStr);
        //去除html标签
        htmlContent = Jsoup.clean(htmlContent, Whitelist.none());
        htmlContent = htmlContent.replaceAll(lineRegex, "").replaceAll(regex, "");
        return htmlContent.trim();
    }
}
