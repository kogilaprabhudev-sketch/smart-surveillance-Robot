package com.surveillance.robot.service;

import com.surveillance.robot.config.TelegramProperties;
import com.surveillance.robot.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends alerts to Telegram on a separate thread so the robot's HTTP request is
 * never blocked (the report requires alerts to run in their own thread).
 */
@Component
public class AlertDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AlertDispatcher.class);

    private final TelegramService telegram;
    private final TelegramProperties props;
    private final AlertRepository alerts;

    public AlertDispatcher(TelegramService telegram, TelegramProperties props, AlertRepository alerts) {
        this.telegram = telegram;
        this.props = props;
        this.alerts = alerts;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAlertCreated(AlertCreatedEvent event) {
        if (props.chatId().isBlank()) {
            log.info("TELEGRAM_CHAT_ID not set - alert {} stored but not sent: {}", event.alertId(), event.message());
            return;
        }
        if (telegram.sendMessage(props.chatId(), event.message())) {
            alerts.markDelivered(event.alertId());
        }
    }
}
