package mj.mycrawler.extractor;

import mj.mycrawler.model.ExtractedPage;
import org.jsoup.nodes.Document;

/**
 * 페이지 데이터 추출 인터페이스
 */
public interface PageExtractor {

    /**
     * 해당 extractor가 이 URL을 처리할 수 있는지 판단
     */
    boolean supports(String url);

    /**
     * Document → ExtractedPage 변환
     */
    ExtractedPage extract(String url, Document doc);
}