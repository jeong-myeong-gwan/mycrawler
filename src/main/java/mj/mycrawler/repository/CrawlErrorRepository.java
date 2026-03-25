package mj.mycrawler.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CrawlErrorRepository {

    private final JdbcTemplate jdbcTemplate;

    public CrawlErrorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 에러 저장
     */
    public void save(
            Long urlId,
            String url,
            String errorType,
            String errorMessage,
            Integer retryCount
    ) {

        String sql = "INSERT INTO crawl_error ( url_id, url,error_type, error_message, retry_count, created_at)  VALUES (?, ?, ?, ?, ?, NOW())";

        jdbcTemplate.update(
                sql,
                urlId,
                url,
                errorType,
                errorMessage,
                retryCount
        );
    }

    /**
     * 특정 URL의 에러 조회
     */
    public List<String> findErrorsByUrlId(Long urlId) {

        String sql = "SELECT error_message FROM crawl_error WHERE url_id = ? ORDER BY created_at DESC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("error_message"),
                urlId
        );
    }

    /**
     * 최근 에러 조회 (모니터링용)
     */
    public List<String> findRecentErrors(int limit) {

        String sql = "SELECT CONCAT('[', created_at, '] ', url, ' | ', error_type, ' | ', error_message) AS msg  FROM crawl_error ORDER BY created_at DESC LIMIT ?";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("msg"),
                limit
        );
    }

    /**
     * 특정 에러 타입 통계
     */
    public int countByErrorType(String errorType) {

        String sql = "SELECT COUNT(*) FROM crawl_error WHERE error_type = ?";

        return jdbcTemplate.queryForObject(sql, Integer.class, errorType);
    }

    /**
     * 오래된 에러 삭제 (데이터 정리용)
     */
    public int deleteOlderThanDays(int days) {

        String sql = "DELETE FROM crawl_error WHERE created_at < NOW() - INTERVAL ? DAY";

        return jdbcTemplate.update(sql, days);
    }
}