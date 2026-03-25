package mj.mycrawler.service;

import mj.mycrawler.extractor.LinkExtractor;
import mj.mycrawler.extractor.PageExtractor;
import mj.mycrawler.fetcher.HttpFetcher;
import mj.mycrawler.model.CrawlResult;
import mj.mycrawler.model.CrawlUrl;
import mj.mycrawler.model.ExtractedPage;
import mj.mycrawler.parser.HtmlParser;
import mj.mycrawler.repository.CrawlConfigRepository;
import mj.mycrawler.repository.CrawlErrorRepository;
import mj.mycrawler.repository.CrawlLogRepository;
import mj.mycrawler.repository.CrawlResultRepository;
import mj.mycrawler.repository.CrawlUrlRepository;
import mj.mycrawler.robots.RobotsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CrawlWorkerService {

    private final HttpFetcher fetcher;
    private final HtmlParser parser;
    private final PageExtractor extractor;
    private final LinkExtractor linkExtractor;
    private final RobotsService robotsService;
    private final CrawlUrlRepository crawlUrlRepository;
    private final CrawlResultRepository crawlResultRepository;
    private final CrawlLogRepository crawlLogRepository;
    private final CrawlErrorRepository crawlErrorRepository;
    private final CrawlConfigRepository crawlConfigRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${crawler.defaultDelayMs}")
    private int defaultDelayMs;

    @Value("${crawler.defaultTimeoutMs}")
    private int defaultTimeoutMs;

    public CrawlWorkerService(
            HttpFetcher fetcher,
            HtmlParser parser,
            PageExtractor extractor,
            LinkExtractor linkExtractor,
            RobotsService robotsService,
            CrawlUrlRepository crawlUrlRepository,
            CrawlResultRepository crawlResultRepository,
            CrawlLogRepository crawlLogRepository,
            CrawlErrorRepository crawlErrorRepository,
            CrawlConfigRepository crawlConfigRepository
    ) {
        this.fetcher = fetcher;
        this.parser = parser;
        this.extractor = extractor;
        this.linkExtractor = linkExtractor;
        this.robotsService = robotsService;
        this.crawlUrlRepository = crawlUrlRepository;
        this.crawlResultRepository = crawlResultRepository;
        this.crawlLogRepository = crawlLogRepository;
        this.crawlErrorRepository = crawlErrorRepository;
        this.crawlConfigRepository = crawlConfigRepository;
    }

    public void process(CrawlUrl crawlUrl) {

        if (!crawlUrlRepository.markRunning(crawlUrl.getId())) {
            return;
        }

        try {
            if (!robotsService.isAllowed(crawlUrl.getUrl())) {
                crawlLogRepository.save(crawlUrl.getId(), crawlUrl.getUrl(), "BLOCKED_ROBOTS", null, "Blocked by robots.txt", null);
                crawlUrlRepository.markFail(crawlUrl.getId());
                return;
            }

            CrawlConfigRepository.CrawlDomainConfig domainConfig =
                    crawlConfigRepository.findByUrl(crawlUrl.getUrl(), defaultDelayMs, defaultTimeoutMs);

            if (!domainConfig.enabled()) {
                crawlLogRepository.save(crawlUrl.getId(), crawlUrl.getUrl(), "DISABLED", null, "Domain disabled", null);
                crawlUrlRepository.markFail(crawlUrl.getId());
                return;
            }

            Thread.sleep(domainConfig.delayMs());

            HttpFetcher.FetchResult response =
                    fetcher.fetch(crawlUrl.getUrl(), domainConfig.userAgent(), domainConfig.timeoutMs());

            if (!response.isSuccess()) {
                throw new RuntimeException("HTTP error: " + response.status);
            }
            
            Document document = parser.parse(response.body, crawlUrl.getUrl());
            if (document == null) {
                throw new RuntimeException("HTML parse failed");
            }	
            
            ExtractedPage extractedPage = extractor.extract(crawlUrl.getUrl(), document);

            CrawlResult result = new CrawlResult();
            result.setUrlId(crawlUrl.getId());
            result.setUrl(crawlUrl.getUrl());
            result.setTitle(extractedPage.getTitle());
            result.setContent(extractedPage.getContent());
            result.setRawHtml(response.body);
            result.setExtractedJson(objectMapper.writeValueAsString(extractedPage.getExtractedJson()));
            result.setStatus("DONE");

            crawlResultRepository.save(result);
            crawlLogRepository.save(crawlUrl.getId(), crawlUrl.getUrl(), "DONE", response.status, null, response.durationMs);
            crawlUrlRepository.markDone(crawlUrl.getId());

            Set<String> discoveredLinks = linkExtractor.extractLinks(crawlUrl.getUrl(), document);

            for (String discoveredUrl : discoveredLinks) {
                if (!robotsService.isAllowed(discoveredUrl)) {
                    continue;
                }

                crawlUrlRepository.insertDiscoveredUrl(
                        discoveredUrl,
                        Math.max(crawlUrl.getPriority() - 1, 0),
                        crawlUrl.getDepth() + 1,
                        crawlUrl.getId()
                );
            }

        } catch (Exception e) {

            int nextRetry = crawlUrl.getRetryCount() + 1;

            crawlLogRepository.save(
                    crawlUrl.getId(),
                    crawlUrl.getUrl(),
                    "FAIL",
                    null,
                    e.getMessage(),
                    null
            );

            crawlErrorRepository.save(
                    crawlUrl.getId(),
                    crawlUrl.getUrl(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    nextRetry
            );

            if (nextRetry >= crawlUrl.getMaxRetry()) {
                crawlUrlRepository.markFail(crawlUrl.getId());
            } else {
                crawlUrlRepository.markRetry(crawlUrl.getId(), nextRetry, calcBackoffSeconds(nextRetry));
            }
        }
    }

    private int calcBackoffSeconds(int retryCount) {
        return switch (retryCount) {
            case 1 -> 30;
            case 2 -> 120;
            default -> 300;
        };
    }
}