package mj.mycrawler.robots;

import mj.mycrawler.model.RobotsRule;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RobotsService {

    private final RobotsTxtParser parser;
    private final Map<String, RobotsRule> cache = new ConcurrentHashMap<>();

    @Value("${crawler.userAgent}")
    private String userAgent;

    @Value("${crawler.defaultTimeoutMs}")
    private int timeoutMs;

    public RobotsService(RobotsTxtParser parser) {
        this.parser = parser;
    }

    public boolean isAllowed(String url) {
        try {
            URI uri = URI.create(url);
            String base = uri.getScheme() + "://" + uri.getHost();
            String path = uri.getPath() == null || uri.getPath().isEmpty() ? "/" : uri.getPath();

            RobotsRule rule = cache.computeIfAbsent(base, this::loadRule);

            String matchedAllow = longestPrefix(rule.getAllowList(), path);
            String matchedDisallow = longestPrefix(rule.getDisallowList(), path);

            if (matchedAllow == null && matchedDisallow == null) {
                return true;
            }

            if (matchedAllow == null) {
                return false;
            }

            if (matchedDisallow == null) {
                return true;
            }

            return matchedAllow.length() >= matchedDisallow.length();

        } catch (Exception e) {
            return true;
        }
    }

    private RobotsRule loadRule(String baseUrl) {
        try {
            String robotsUrl = baseUrl + "/robots.txt";
            String robotsText = Jsoup.connect(robotsUrl)
                    .userAgent(userAgent)
                    .timeout(timeoutMs)
                    .ignoreContentType(true)
                    .execute()
                    .body();

            return parser.parse(robotsText, userAgent);
        } catch (Exception e) {
            return new RobotsRule();
        }
    }

    private String longestPrefix(Iterable<String> list, String path) {
        String best = null;
        for (String candidate : list) {
            if (path.startsWith(candidate)) {
                if (best == null || candidate.length() > best.length()) {
                    best = candidate;
                }
            }
        }
        return best;
    }
}