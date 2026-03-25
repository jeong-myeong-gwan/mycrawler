package mj.mycrawler.model;

import java.time.LocalDateTime;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlUrl {

    private long id;
    private String url;
    private String status;
    private int retryCount;
    private int maxRetry;
    private int priority;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastCrawledAt;
    private int depth;
    private Long parentUrlId;

    
}