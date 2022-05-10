package com.opengauss.utils;

import com.opengauss.constant.TypeConstants;
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

    public static String[] parseHtml(String mdStr, String type,String fileName) {

        String[] result = new String[12];
        String mdTrim = mdStr.replaceAll(lineRegex, "");

        Parser parser = Parser.builder().build();
        Node document = parser.parse(mdStr);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        String r = Jsoup.parse(renderer.render(document)).text();


        String textContent = "";
        String title = "";

        if (TypeConstants.DOCS.equals(type)) {

            String m = renderer.render(document);
            if (m.contains("<h1>")) {
                title = m.substring(m.indexOf("<h1>") + 4);
                title = Jsoup.parse(title.substring(0, title.indexOf("</h1>"))).text();
                textContent = m.substring(m.indexOf("</h1>") + 5);
                textContent = Jsoup.parse(textContent).text().replaceAll(lineRegex, "").replaceAll(regex, "");
            } else {
                title = fileName;
                textContent = r.replaceAll(lineRegex, "");
            }

        } else {
            textContent = r.replaceAll(lineRegex, "").replaceAll(regex, "");
            title = getValue(mdTrim, "title=\"\"");
        }
        result[0] = textContent;
        result[2] = title;

        return result;
    }



    public static String getValue(String r, String t) {

        String begin = t.substring(0, t.length() - 1);
        String last = t.substring(t.length()-1);


        if (last.equals("\"")) {
            if (r.contains(begin)) {
                t = r.substring(r.indexOf(begin) + begin.length());
                t = t.substring(0, t.indexOf(last));
            }

            return t;
        }

        if (last.equals("]")) {
            if (r.contains(begin)) {
                t = r.substring(r.indexOf(begin) + begin.length());
                t = t.substring(0,  t.indexOf(last));
                t = t.replaceAll("\"", "").replaceAll("\\s*", "");
                return t;
            }
        }

        return "";
    }
}

