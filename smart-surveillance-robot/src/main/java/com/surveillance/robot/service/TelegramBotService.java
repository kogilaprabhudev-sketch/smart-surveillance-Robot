package com.surveillance.robot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.surveillance.robot.config.TelegramProperties;
import com.surveillance.robot.dto.RobotResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Telegram command bot (Java version of iot_receive.py).
 * Commands: /status /last /time /date /help. Only the configured chat may use it.
 */
@Component
public class TelegramBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final TelegramService telegram;
    private final TelegramProperties props;
    private final RobotService robotService;
    private final DetectionService detectionService;
    private final ZoneId zone;

    private volatile boolean running;
    private Thread worker;

    public TelegramBotService(TelegramService telegram, TelegramProperties props, RobotService robotService,
                              DetectionService detectionService,
                              @Value("${app.timezone:Asia/Kolkata}") String timezone) {
        this.telegram = telegram;
        this.props = props;
        this.robotService = robotService;
        this.detectionService = detectionService;
        this.zone = ZoneId.of(timezone);
    }

    @PostConstruct
    void start() {
        if (!telegram.isConfigured() || !props.pollingEnabled()) {
            log.info("Telegram bot disabled (no token or polling turned off)");
            return;
        }
        if (props.chatId().isBlank()) {
            log.warn("TELEGRAM_CHAT_ID not set - bot commands stay disabled so strangers cannot query the robot");
            return;
        }
        running = true;
        worker = new Thread(this::pollLoop, "telegram-poller");
        worker.setDaemon(true);
        worker.start();
        log.info("Telegram bot is running");
    }

    @PreDestroy
    void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }

    private void pollLoop() {
        long offset = 0;
        while (running) {
            try {
                JsonNode updates = telegram.getUpdates(offset, 25);
                for (JsonNode update : updates) {
                    offset = update.path("update_id").asLong() + 1;
                    handle(update);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Telegram polling error: {}", e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void handle(JsonNode update) {
        JsonNode msg = update.path("message");
        if (msg.isMissingNode() || !msg.has("text")) {
            return;   // ignore non-text messages
        }
        String chatId = msg.path("chat").path("id").asText();
        if (!chatId.equals(props.chatId())) {
            log.warn("Ignoring message from an unauthorized chat");
            return;
        }
        String cmd = msg.path("text").asText().trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
        int at = cmd.indexOf('@');
        if (at > 0) {
            cmd = cmd.substring(0, at);
        }
        String reply = switch (cmd) {
            case "/start", "/help" -> "Commands: /status, /last, /time, /date";
            case "/status" -> statusText();
            case "/last" -> lastDetectionText();
            case "/time" -> "Time: " + ZonedDateTime.now(zone).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "/date" -> "Date: " + ZonedDateTime.now(zone).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            default -> "Unknown command";
        };
        telegram.sendMessage(chatId, reply);
    }

    private String statusText() {
        var robots = robotService.list();
        if (robots.isEmpty()) {
            return "No robots registered";
        }
        return robots.stream().map(this::describe).collect(Collectors.joining("\n"));
    }

    private String describe(RobotResponse r) {
        String where = (r.latitude() == null || r.longitude() == null) ? "location unknown"
                : String.format(Locale.ROOT, "at %.5f, %.5f", r.latitude(), r.longitude());
        String seen = r.lastSeenAt() == null ? "never seen"
                : "last seen " + r.lastSeenAt().atZone(zone).format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"));
        return r.robotCode() + ": " + r.status() + ", " + seen + ", " + where;
    }

    private String lastDetectionText() {
        return detectionService.latest()
                .map(d -> String.format(Locale.ROOT, "Last detection: %s (%.0f%%) by %s at %s",
                        d.objectClass(), d.confidence() * 100, d.robotCode(),
                        d.detectedAt().atZone(zone).format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"))))
                .orElse("No detections yet");
    }
}
