package mj.mycrawler.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.*;

/**
 * HTML에서 추출된 구조화 데이터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedPage {

    private String url;

    private String title;
    private String content;

    private List<String> links; // 발견된 링크

    private Map<String, Object> extractedJson; // 추가 메타데이터

    
}