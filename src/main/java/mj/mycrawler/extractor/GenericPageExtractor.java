package mj.mycrawler.extractor;

import mj.mycrawler.model.ExtractedPage;
import mj.mycrawler.parser.HtmlParser;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 기본 페이지 추출기 (fallback)
 */
@Component
public class GenericPageExtractor implements PageExtractor {

    private final HtmlParser parser;

    public GenericPageExtractor(HtmlParser parser) {
        this.parser = parser;
    }

    /**
     * 모든 URL 처리 (fallback)
     */
    @Override
    public boolean supports(String url) {
        return true;
    }

    /**
     * HTML → ExtractedPage 변환
     */
    @Override
    public ExtractedPage extract(String url, Document doc) {

        if (doc == null) {
            return empty(url);
        }

        String title = parser.extractTitle(doc);
        String content = parser.extractBodyText(doc);
        String description = parser.extractMetaDescription(doc);
        List<String> links = parser.extractLinks(doc);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", description);

        ExtractedPage page = new ExtractedPage();
        page.setUrl(url);
        page.setTitle(title);
        page.setContent(content);
        page.setLinks(links);
        page.setExtractedJson(metadata);

        return page;
    }

    private ExtractedPage empty(String url) {
        ExtractedPage page = new ExtractedPage();
        page.setUrl(url);
        return page;
    }
}