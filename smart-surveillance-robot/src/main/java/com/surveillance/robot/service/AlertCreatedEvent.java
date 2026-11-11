package com.surveillance.robot.service;

/** Published after an alert row is saved; delivered to Telegram once the transaction commits. */
public record AlertCreatedEvent(Long alertId, String message) {
}
