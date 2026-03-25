package mj.mycrawler.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * HTML 파싱 담당
 */
@Component
public class HtmlParser {

    /**
     * HTML → Document
     */
    public Document parse(String html, String baseUrl) {
        if (html == null || html.isBlank()) {
            return null;
        }

        return Jsoup.parse(html, baseUrl);
    }

    /**
     * title 추출
     */
    public String extractTitle(Document doc) {
        if (doc == null) return null;
        return doc.title();
    }

    /**
     * meta description 추출
     */
    public String extractMetaDescription(Document doc) {
        if (doc == null) return null;

        Element meta = doc.selectFirst("meta[name=description]");
        if (meta != null) {
            return meta.attr("content");
        }
        return null;
    }

    /**
     * 본문 텍스트 추출
     */
    public String extractBodyText(Document doc) {
        if (doc == null) return null;

        // script / style 제거
        doc.select("script, style, noscript").remove();

        Element body = doc.body();
        if (body == null) return null;

        return body.text();
    }

    /**
     * 링크 추출 (절대 URL)
     */
    public List<String> extractLinks(Document doc) {
        List<String> links = new ArrayList<>();

        if (doc == null) return links;

        Elements elements = doc.select("a[href]");

        for (Element el : elements) {
            String absUrl = el.absUrl("href");

            if (absUrl == null || absUrl.isBlank()) {
                continue;
            }

            // http/https만 허용
            if (absUrl.startsWith("http://") || absUrl.startsWith("https://")) {
                links.add(absUrl);
            }
        }

        return links;
    }
}