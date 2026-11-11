package com.surveillance.robot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surveillance.robot.config.TelegramProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper over the Telegram Bot HTTP API (replaces the telepot library
 * used in the Python version). The bot token is never logged.
 */
@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    private final TelegramProperties props;
    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public TelegramService(TelegramProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
    }

    public boolean isConfigured() {
        return !props.botToken().isBlank();
    }

    /** Sends a text message. Returns true when Telegram accepted it. */
    public boolean sendMessage(String chatId, String text) {
        if (!isConfigured()) {
            log.info("Telegram not configured - message not sent: {}", text);
            return false;
        }
        try {
            String body = mapper.writeValueAsString(Map.of("chat_id", chatId, "text", text));
            HttpRequest request = HttpRequest.newBuilder(URI.create(url("sendMessage")))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.warn("Telegram sendMessage failed with HTTP {}", response.statusCode());
                return false;
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("Telegram sendMessage error: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    /** Long-polls for new messages (used for the /status, /time, /date bot commands). */
    public JsonNode getUpdates(long offset, int timeoutSeconds) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(url("getUpdates") + "?timeout=" + timeoutSeconds + "&offset=" + offset))
                .timeout(Duration.ofSeconds(timeoutSeconds + 10L))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Telegram getUpdates returned HTTP " + response.statusCode());
        }
        return mapper.readTree(response.body()).path("result");
    }

    private String url(String method) {
        return "https://api.telegram.org/bot" + props.botToken() + "/" + method;
    }
}
