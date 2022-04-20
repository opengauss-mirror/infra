package com.opengauss.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.pegdown.PegDownProcessor;


public class ParseHtmlUtil {
    private static final String lineRegex = "\\s*|\t|\r|\n";
    private static final String regex = "\\+\\+\\+(.*?)\\+\\+\\+";

    public static String parseHtml(String mdStr) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(mdStr);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String r = Jsoup.parse(renderer.render(document)).text();

        return r.replaceAll(lineRegex, "").replaceAll(regex, "");


    }
}
