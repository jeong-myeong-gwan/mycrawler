package mj.mycrawler.model;

import java.time.LocalDateTime;
import lombok.*;


/**
 * 크롤링 결과 저장 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResult {

    private Long id;
    private Long urlId;
    private String url;

    private String title;
    private String content;

    private String rawHtml;        // 원본 HTML
    private String extractedJson;  // JSON 문자열

    private String status;         // DONE / FAIL

    private LocalDateTime crawledAt;

    
}