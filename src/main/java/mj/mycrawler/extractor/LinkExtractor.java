package mj.mycrawler.extractor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class LinkExtractor {

    @Value("${crawler.maxDiscoveredLinksPerPage}")
    private int maxDiscoveredLinksPerPage;

    @Value("${crawler.followExternalLinks}")
    private boolean followExternalLinks;

    public Set<String> extractLinks(String currentUrl, Document document) {
        Set<String> links = new LinkedHashSet<>();

        URI baseUri = URI.create(currentUrl);
        String currentHost = baseUri.getHost();

        for (Element a : document.select("a[href]")) {
            if (links.size() >= maxDiscoveredLinksPerPage) {
                break;
            }

            String absUrl = a.attr("abs:href");
            if (absUrl == null || absUrl.isBlank()) {
                continue;
            }

            if (!(absUrl.startsWith("http://") || absUrl.startsWith("https://"))) {
                continue;
            }

            try {
                URI linkUri = URI.create(absUrl);

                if (!followExternalLinks) {
                    if (linkUri.getHost() == null || !linkUri.getHost().equalsIgnoreCase(currentHost)) {
                        continue;
                    }
                }

                String normalized = normalize(absUrl);
                if (!normalized.isBlank()) {
                    links.add(normalized);
                }

            } catch (Exception ignored) {
            }
        }

        return links;
    }

    private String normalize(String url) {
        try {
            URI uri = URI.create(url);
            URI normalized = new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    null,
                    null
            );
            return normalized.toString();
        } catch (Exception e) {
            return url;
        }
    }
}