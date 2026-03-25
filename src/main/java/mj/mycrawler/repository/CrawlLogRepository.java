package mj.mycrawler.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CrawlLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public CrawlLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 크롤링 로그 저장
     */
    public void save(
            Long urlId,
            String url,
            String status,
            Integer httpStatus,
            String errorMessage,
            Long durationMs
    ) {

        String sql = "INSERT INTO crawl_log (url_id, url, status, http_status, error_message, duration_ms) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                urlId,
                url,
                status,
                httpStatus,
                errorMessage,
                durationMs
        );
    }
}