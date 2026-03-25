package mj.mycrawler.scheduler;

import mj.mycrawler.model.CrawlUrl;
import mj.mycrawler.repository.CrawlUrlRepository;
import mj.mycrawler.service.CrawlWorkerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CrawlScheduler {

    private final ScheduledExecutorService schedulerExecutor;
    private final ExecutorService workerExecutor;
    private final long schedulerDelayMs;
    private final CrawlUrlRepository crawlUrlRepository;
    private final CrawlWorkerService crawlWorkerService;

    public CrawlScheduler(
    		@Qualifier("scheduledExecutorService") ScheduledExecutorService schedulerExecutor,
    		@Qualifier("workerExecutorService") ExecutorService workerExecutor,
            @Qualifier("schedulerDelayMs") Long schedulerDelayMs,
            CrawlUrlRepository crawlUrlRepository,
            CrawlWorkerService crawlWorkerService
    ) {
        this.schedulerExecutor = schedulerExecutor;
        this.workerExecutor = workerExecutor;
        this.schedulerDelayMs = schedulerDelayMs;
        this.crawlUrlRepository = crawlUrlRepository;
        this.crawlWorkerService = crawlWorkerService;
    }

    public void start() {
        schedulerExecutor.scheduleWithFixedDelay(this::runOnce, 0, schedulerDelayMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        schedulerExecutor.shutdown();
        workerExecutor.shutdown();
    }

    private void runOnce() {
        try {
            crawlUrlRepository.resetStuckRunningTasks();

            List<CrawlUrl> urls = crawlUrlRepository.findDueUrls();

            for (CrawlUrl crawlUrl : urls) {
                workerExecutor.submit(() -> crawlWorkerService.process(crawlUrl));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}