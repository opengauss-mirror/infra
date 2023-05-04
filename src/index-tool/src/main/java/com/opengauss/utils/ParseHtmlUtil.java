package com.opengauss.utils;

import com.opengauss.constant.TypeConstants;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.jsoup.Jsoup;
import org.pegdown.PegDownProcessor;


public class ParseHtmlUtil {
    private static final String lineRegex = "\\s*|\t|\r|\n";
    private static final String regex = "\\+\\+\\+(.*?)\\+\\+\\+";

    public static String[] parseHtml(String mdStr, String type) {

        String[] result = new String[12];

        Parser parser = Parser.builder().build();
        Node document = parser.parse(mdStr);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        String r = Jsoup.parse(renderer.render(document)).text();


        String textContent = "";
        String title = "";
        textContent = Jsoup.parse(r).text();
        if (TypeConstants.DOCS.equals(type)) {
            if (textContent.contains(" ")) {
                title = textContent.substring(0, textContent.indexOf(" "));
            } else {
                title = textContent;
            }

        } else {
            textContent = r.replaceAll(regex, "");
            title = getValue(mdStr, "title");
        }
        result[0] = textContent;
        result[2] = title;

        return result;
    }



    public static String getValue(String r, String t) {
        String result = "";

        for(int i = 0; i < r.length(); i ++) {
            if (r.contains(t)) {
                r = r.substring(r.indexOf(t) + t.length());
                if (r.substring(0, 4).contains("=")) {
                    result = r.substring(r.indexOf("=") + 1);
                    if (t.equals("tags") || t.equals("categories")) {
                        result = result.substring(result.indexOf("[") + 1);
                        result = result.substring(0, result.indexOf("]"));
                        result = result.replaceAll("'", "");
                        break;
                    } else {
                        if (result.substring(0, 3).contains("'")) {
                            result = result.substring(result.indexOf("'") + 1);
                            result = result.substring(0, result.indexOf("'"));
                        } else if (result.substring(0, 3).contains("\"")){
                            result = result.substring(result.indexOf("\"") + 1);
                            result = result.substring(0, result.indexOf("\""));
                        }
                        break;
                    }
                }
            } else {
                break;
            }
        }


        return result;
    }
}

