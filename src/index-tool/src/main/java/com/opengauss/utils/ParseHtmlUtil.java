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

        TextContentRenderer textContentRenderer = TextContentRenderer.builder().build();
        //This is Sparta
        return textContentRenderer.render(document).replaceAll(lineRegex, "").replaceAll(regex, "");


    }
}
