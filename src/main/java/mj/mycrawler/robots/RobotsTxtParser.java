package mj.mycrawler.robots;

import mj.mycrawler.model.RobotsRule;
import org.springframework.stereotype.Component;

@Component
public class RobotsTxtParser {

    public RobotsRule parse(String robotsText, String userAgent) {
        RobotsRule rule = new RobotsRule();

        if (robotsText == null || robotsText.isBlank()) {
            return rule;
        }

        String[] lines = robotsText.split("\\r?\\n");

        boolean matchedGroup = false;
        boolean wildcardGroup = false;

        for (String raw : lines) {
            String line = raw.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String lower = line.toLowerCase();

            if (lower.startsWith("user-agent:")) {
                String ua = line.substring("user-agent:".length()).trim();
                matchedGroup = ua.equalsIgnoreCase(userAgent);
                wildcardGroup = ua.equals("*");
                continue;
            }

            if (!(matchedGroup || wildcardGroup)) {
                continue;
            }

            if (lower.startsWith("allow:")) {
                String path = line.substring("allow:".length()).trim();
                if (!path.isEmpty()) {
                    rule.getAllowList().add(path);
                }
            } else if (lower.startsWith("disallow:")) {
                String path = line.substring("disallow:".length()).trim();
                if (!path.isEmpty()) {
                    rule.getDisallowList().add(path);
                }
            }
        }

        return rule;
    }
}