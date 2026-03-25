package mj.mycrawler.repository;

import mj.mycrawler.model.CrawlResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CrawlResultRepository {

    private final JdbcTemplate jdbcTemplate;

    public CrawlResultRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 크롤링 결과 저장
     */
    public void save(CrawlResult result) {

        String sql = "INSERT INTO crawl_result (url_id, url, title, content, raw_html, extracted_json, status) VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), ?)";

        jdbcTemplate.update(
                sql,
                result.getUrlId(),
                result.getUrl(),
                result.getTitle(),
                result.getContent(),
                result.getRawHtml(),
                result.getExtractedJson(), // JSON 문자열
                result.getStatus()
        );
    }
}