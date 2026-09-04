package com.task.scheduler.core.DTOs;

import com.task.scheduler.core.domain.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(UUID id,
                          String uniqueKey,
                          String queueName,
                          TaskStatus status,
                          int priority,
                          int currentRetry,
                          int maxRetries,
                          String payload,
                          String lastError,
                          Instant executeAt,
                          Instant createdAt,
                          Instant updatedAt)
{}
