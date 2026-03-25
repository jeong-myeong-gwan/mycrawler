package mj.mycrawler.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * DB 연결 설정 (HikariCP + JdbcTemplate + Transaction)
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Value("${db.url}")
    private String jdbcUrl;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Value("${db.pool.maxSize:10}")
    private int maxPoolSize;

    @Value("${db.pool.minIdle:2}")
    private int minIdle;

    @Value("${db.pool.connectionTimeout:3000}")
    private long connectionTimeout;

    /**
     * 1️⃣ DataSource (HikariCP)
     */
    @Bean
    public DataSource dataSource() {

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(username);
        cfg.setPassword(password);

        // Pool 설정
        cfg.setMaximumPoolSize(maxPoolSize);
        cfg.setMinimumIdle(minIdle);
        cfg.setConnectionTimeout(connectionTimeout);

        // MySQL 권장 옵션
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 성능 옵션
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        cfg.addDataSourceProperty("useServerPrepStmts", "true");
        cfg.addDataSourceProperty("rewriteBatchedStatements", "true");
        cfg.setIdleTimeout(600000);        // 10분
        cfg.setMaxLifetime(1800000);       // 30분
        cfg.setValidationTimeout(3000);

        return new HikariDataSource(cfg);
    }

    /**
     * 2️⃣ JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 3️⃣ Transaction Manager
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}