package mj.mycrawler.repository;

import mj.mycrawler.model.CrawlUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class CrawlUrlRepository {

    private final JdbcTemplate jdbcTemplate;

    @Value("${crawler.batchSize}")
    private int batchSize;

    @Value("${crawler.maxRetry}")
    private int defaultMaxRetry;

    public CrawlUrlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CrawlUrl> findDueUrls() {
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT id, url, status, retry_count, max_retry, priority, ")
        	.append("next_run_at, last_crawled_at, depth, parent_url_id ")
        	.append("FROM crawl_url ")
        	.append("WHERE status = 'WAIT' ")
        	.append("AND next_run_at <= NOW() ")
        	.append("AND retry_count < max_retry ")
        	.append("ORDER BY priority DESC, id ASC ")
        	.append("LIMIT ? ");

        String sql = sqlBuilder.toString();
        
        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            CrawlUrl c = new CrawlUrl();
            c.setId(rs.getLong("id"));
            c.setUrl(rs.getString("url"));
            c.setStatus(rs.getString("status"));
            c.setRetryCount(rs.getInt("retry_count"));
            c.setMaxRetry(rs.getInt("max_retry"));
            c.setPriority(rs.getInt("priority"));
            c.setDepth(rs.getInt("depth"));

            long parentId = rs.getLong("parent_url_id");
            if (!rs.wasNull()) {
                c.setParentUrlId(parentId);
            }

            if (rs.getTimestamp("next_run_at") != null) {
                c.setNextRunAt(rs.getTimestamp("next_run_at").toLocalDateTime());
            }
            if (rs.getTimestamp("last_crawled_at") != null) {
                c.setLastCrawledAt(rs.getTimestamp("last_crawled_at").toLocalDateTime());
            }

            return c;
        }, batchSize);
    }

    public boolean markRunning(long id) {
        String sql = "UPDATE crawl_url SET status = 'RUNNING' WHERE id = ? AND status = 'WAIT'";
        return jdbcTemplate.update(sql, id) == 1;
    }

    public void markDone(long id) {
        String sql = "UPDATE crawl_url SET status = 'DONE', last_crawled_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void markRetry(long id, int retryCount, int delaySeconds) {
        String sql = "UPDATE crawl_url SET status = 'WAIT', retry_count = ?, next_run_at = DATE_ADD(NOW(), INTERVAL ? SECOND) WHERE id = ?";
        jdbcTemplate.update(sql, retryCount, delaySeconds, id);
    }

    public void markFail(long id) {
        String sql = "UPDATE crawl_url SET status = 'FAIL' WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void resetStuckRunningTasks() {
        String sql = "UPDATE crawl_url SET status = 'WAIT' WHERE status = 'RUNNING' AND last_crawled_at IS NULL";
        jdbcTemplate.update(sql);
    }

    public void insertDiscoveredUrl(String url, int priority, int depth, Long parentUrlId) {
        String sql = "INSERT IGNORE INTO crawl_url (url, status, retry_count, max_retry, priority, depth, parent_url_id) VALUES (?, 'WAIT', 0, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, url, defaultMaxRetry, priority, depth, parentUrlId);
    }
}