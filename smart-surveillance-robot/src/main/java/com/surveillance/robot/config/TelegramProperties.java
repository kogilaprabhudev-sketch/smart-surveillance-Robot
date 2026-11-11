package com.surveillance.robot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(String botToken, String chatId, boolean pollingEnabled) {

    public TelegramProperties {
        botToken = botToken == null ? "" : botToken.trim();
        chatId = chatId == null ? "" : chatId.trim();
    }
}
