package mj.mycrawler;

import mj.mycrawler.config.AppConfig;
import mj.mycrawler.scheduler.CrawlScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CountDownLatch;

public class MainApp {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {

        log.info("=======================================");
        log.info(" MyCrawler Starting...");
        log.info("=======================================");

        AnnotationConfigApplicationContext ctx = null;

        try {
            // 1️⃣ Spring Context 초기화
            ctx = new AnnotationConfigApplicationContext(AppConfig.class);

            // 2️⃣ Scheduler Bean 가져오기
            CrawlScheduler scheduler = ctx.getBean(CrawlScheduler.class);

            // 3️⃣ Scheduler 시작
            scheduler.start();

            log.info("Crawler Scheduler started.");

            // 4️⃣ JVM 종료 방지 (메인 쓰레드 대기)
            CountDownLatch latch = new CountDownLatch(1);

            // 5️⃣ 종료 Hook 등록
            AnnotationConfigApplicationContext finalCtx = ctx;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutdown signal received.");

                try {
                    scheduler.stop();
                    log.info("Scheduler stopped.");

                    if (finalCtx != null) {
                        finalCtx.close();
                        log.info("Spring context closed.");
                    }

                } catch (Exception e) {
                    log.error("Error during shutdown", e);
                }

                latch.countDown();
            }));

            // 6️⃣ 메인 쓰레드 block
            log.info("Crawler is running... (Press Ctrl+C to stop)");
            latch.await();

        } catch (Exception e) {
            log.error("Fatal error during startup", e);

            if (ctx != null) {
                ctx.close();
            }
        }
    }
}