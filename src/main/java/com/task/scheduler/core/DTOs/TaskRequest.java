package com.task.scheduler.core.DTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.Instant;

@Builder
public record TaskRequest(

        @NotBlank(message = "Unique key is required")
        String uniqueKey,

        @NotBlank(message = "Queue name is required")
        String queueName,

        @NotBlank(message = "Payload is required")
        String payload,

        @Min(1) @Max(10)
        int priority,

        @Min(0) @Max(10)
        int maxRetries,

        Instant executeAt
) {
    public TaskRequest {
        if (queueName == null || queueName.isBlank())
            queueName = "default";
        if (priority == 0)
            priority = 5;
        if (executeAt == null)
            executeAt = Instant.now();
    }
}
