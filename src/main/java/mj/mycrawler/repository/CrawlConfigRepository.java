package mj.mycrawler.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CrawlConfigRepository {

    private final JdbcTemplate jdbcTemplate;

    public CrawlConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * URL 기반으로 도메인 설정 조회
     * 없으면 default 값 사용
     */
    public CrawlDomainConfig findByUrl(
            String url,
            int defaultDelayMs,
            int defaultTimeoutMs
    ) {

        String domain = extractDomain(url);

        String sql = "SELECT domain, user_agent, delay_ms, timeout_ms, enabled FROM crawl_config WHERE domain = ?";

        return jdbcTemplate.query(
                sql,
                rs -> {
                    if (rs.next()) {
                        return new CrawlDomainConfig(
                                rs.getString("domain"),
                                rs.getString("user_agent"),
                                rs.getInt("delay_ms"),
                                rs.getInt("timeout_ms"),
                                rs.getBoolean("enabled")
                        );
                    } else {
                        // 설정 없으면 default 반환
                        return new CrawlDomainConfig(
                                domain,
                                defaultUserAgent(),
                                defaultDelayMs,
                                defaultTimeoutMs,
                                true
                        );
                    }
                },
                domain
        );
    }

    /**
     * 도메인 설정 저장 (upsert)
     */
    public void saveOrUpdate(
            String domain,
            String userAgent,
            int delayMs,
            int timeoutMs,
            boolean enabled
    ) {

        String sql = "INSERT INTO crawl_config (domain, user_agent, delay_ms, timeout_ms, enabled) VALUES (?, ?, ?, ?, ?) ";
        sql = sql + "ON DUPLICATE KEY UPDATE user_agent = VALUES(user_agent),delay_ms = VALUES(delay_ms),timeout_ms = VALUES(timeout_ms), enabled = VALUES(enabled)";

        jdbcTemplate.update(
                sql,
                domain,
                userAgent,
                delayMs,
                timeoutMs,
                enabled
        );
    }

    /**
     * 도메인 비활성화
     */
    public void disableDomain(String domain) {
        jdbcTemplate.update(
                "UPDATE crawl_config SET enabled = false WHERE domain = ?",
                domain
        );
    }

    /**
     * 도메인 활성화
     */
    public void enableDomain(String domain) {
        jdbcTemplate.update(
                "UPDATE crawl_config SET enabled = true WHERE domain = ?",
                domain
        );
    }

    /**
     * 기본 User-Agent
     */
    private String defaultUserAgent() {
        return "Mozilla/5.0 (compatible; MyCrawler/1.0)";
    }

    /**
     * URL → domain 추출
     */
    private String extractDomain(String url) {
        try {
            return new java.net.URI(url).getHost();
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 내부 DTO (record 사용 - Java 17)
     */
    public record CrawlDomainConfig(
            String domain,
            String userAgent,
            int delayMs,
            int timeoutMs,
            boolean enabled
    ) {
    }
}
