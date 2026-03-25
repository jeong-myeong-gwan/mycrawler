package mj.mycrawler.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


@Configuration
@ComponentScan(basePackages = "mj.mycrawler")
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(
        value = "classpath:application-${spring.profiles.active}.properties",
        ignoreResourceNotFound = true
    )
})
@Import({
        DataSourceConfig.class,
        ExecutorConfig.class
})
public class AppConfig {

	@Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
