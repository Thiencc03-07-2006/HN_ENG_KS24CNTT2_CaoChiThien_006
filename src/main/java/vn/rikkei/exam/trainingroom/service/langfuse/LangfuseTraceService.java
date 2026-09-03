package vn.rikkei.exam.trainingroom.service.langfuse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import vn.rikkei.exam.trainingroom.dto.ChatResponse;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LangfuseTraceService {
    private static final Logger log = LoggerFactory.getLogger(LangfuseTraceService.class);
    private final String publicKey;
    private final String secretKey;
    private final RestClient client;

    public LangfuseTraceService(@Value("${langfuse.host:http://localhost:3000}") String host,
                                @Value("${langfuse.public-key:}") String publicKey,
                                @Value("${langfuse.secret-key:}") String secretKey) {
        this.publicKey = publicKey;
        this.secretKey = secretKey;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.client = RestClient.builder().baseUrl(host).requestFactory(factory).build();
    }

    public void trace(ChatResponse response) {
        if (publicKey == null || publicKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            log.debug("event=langfuse_trace_skipped reason=credentials_not_configured conversationId={}",
                    response != null ? response.getConversationId() : null);
            return;
        }
        if (response == null) return;
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> event = createTraceEvent(response, traceId, Instant.now());
        try {
            client.post().uri("/api/public/ingestion").headers(h -> h.setBasicAuth(publicKey, secretKey))
                    .body(Map.of("batch", List.of(event))).retrieve().toBodilessEntity();
            log.info("event=langfuse_trace_sent conversationId={} traceId={}", response.getConversationId(), traceId);
        } catch (Exception ex) {
            log.warn("event=langfuse_trace_failed conversationId={} exceptionType={}", response.getConversationId(), ex.getClass().getSimpleName());
        }
    }

    static Map<String, Object> createTraceEvent(ChatResponse response, String traceId, Instant timestamp) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("conversationId", response.getConversationId());
        metadata.put("examCode", "DE-006");
        metadata.put("sources", response.getSources() != null ? response.getSources() : Collections.emptyList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", traceId);
        body.put("name", "assistant-chat");
        body.put("sessionId", response.getConversationId());
        body.put("metadata", metadata);
        return Map.of(
                "id", UUID.randomUUID().toString(),
                "type", "trace-create",
                "timestamp", timestamp.toString(),
                "body", body);
    }
}