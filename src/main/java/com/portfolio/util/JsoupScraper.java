package com.portfolio.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class JsoupScraper {
    public static String scrap(String url, String selector) throws IOException {
        Document doc = Jsoup.connect(url).get();
        if (selector == null || selector.isEmpty()) {
            return doc.toString();
        }
        Elements elements;
        String selectorType = getXpathOrSelector(selector);
        int isText = 0;
        int isAttr = 0;
        String attr = "";
        if (selectorType.equals("selector")) {
            elements = doc.select(selector);
        } else if (selectorType.equals("xpath")) {
            isText = selector.contains("/text()") ? selector.contains("//text()") ? 2 : 1 : 0;
            isAttr = selector.contains("/@") ? selector.contains("//@") ? 2 : 1 : 0;
            if (isText > 0) {
                StringBuilder slash = new StringBuilder();
                for (int i = 0; i < isText; i++) {
                    slash.append("/");
                }
                selector = selector.replace(slash.append("text()").toString(), "");
            } else if (isAttr > 0) {
                if (isAttr == 1) {
                    attr = selector.substring(selector.indexOf("/@") + 2);
                    selector = selector.substring(0, selector.indexOf("/@"));
                } else {
                    attr = selector.substring(selector.indexOf("//@") + 3);
                    selector = selector.substring(0, selector.indexOf("//@"));
                }
            }
            elements = doc.selectXpath(selector);
        } else {
            throw new IllegalArgumentException("selector or xpath is required");
        }

        StringBuilder sb = new StringBuilder();

        for (Element element : elements) {
            if (isText > 0) {
                if (isText == 1) {
                    sb.append(element.ownText()).append("\n");
                } else {
                    sb.append(element.text()).append("\n");
                }
            } else if (isAttr > 0) {
                if (isAttr == 1) {
                    if (element.hasAttr(attr) && !element.attr(attr).isEmpty()) {
                        sb.append(element.attr(attr)).append("\n");
                    }
                } else {
                    for (Element allElement : element.getAllElements()) {
                        if (allElement.hasAttr(attr) && !allElement.attr(attr).isEmpty()) {
                            sb.append(allElement.attr(attr)).append("\n");
                        }
                    }
                }
            } else {
                sb.append(element.getAllElements()).append("\n");
            }
        }
        return sb.toString();
    }

    public static String getXpathOrSelector(String selector) {
        if (selector.startsWith("/")) {
            return "xpath";
        }
        return "selector";
    }
}
