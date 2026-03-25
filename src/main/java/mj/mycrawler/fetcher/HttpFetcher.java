package mj.mycrawler.fetcher;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 요청 수행 (OkHttp 기반)
 */
@Component
public class HttpFetcher {

    private final OkHttpClient client;

    @Value("${crawler.userAgent:MyCrawler/1.0}")
    private String userAgent;

    public HttpFetcher(
            @Value("${crawler.timeout.ms:5000}") int timeout
    ) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 결과 객체
     */
    public static class FetchResult {
        public int status;
        public String body;
        public long durationMs;
        public String error;

        public boolean isSuccess() {
            return status >= 200 && status < 300;
        }
    }

    /**
     * GET 요청
     */
    public FetchResult fetch(String url, String userAgent, int timeoutMs) {

        OkHttpClient customClient = client.newBuilder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        FetchResult result = new FetchResult();
        long start = System.currentTimeMillis();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", userAgent)
                //.addHeader("Accept-Encoding", "gzip")
                .build();

        try (Response response = customClient.newCall(request).execute()) {
            result.status = response.code();

            if (response.body() != null) {

			    byte[] bytes = response.body().bytes();
			
			    MediaType contentType = response.body().contentType();
			
			    Charset charset = StandardCharsets.UTF_8;
			
			    if (contentType != null && contentType.charset() != null) {
			        charset = contentType.charset();
			    }
			
			    result.body = new String(bytes, charset);
			}

        } catch (Exception e) {
            result.error = e.getMessage();
        }

        result.durationMs = System.currentTimeMillis() - start;
        return result;
    }
}