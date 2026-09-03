package com.task.scheduler.core.service;

import com.task.scheduler.core.domain.Task;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TaskProducerService {

    private final StringRedisTemplate redisTemplate;

    public TaskProducerService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RecordId publishToStream(Task task) {
        Map<String, String> streamData = Map.of(
                "taskId", task.getId().toString(),
                "payload", task.getPayload(),
                "retryCount", String.valueOf(task.getCurrentRetry()) );

        ObjectRecord<String, Map<String, String>> record = StreamRecords.newRecord()
                .in(task.getQueueName())
                .ofObject(streamData);
        return redisTemplate.opsForStream().add(record);
    }
}
