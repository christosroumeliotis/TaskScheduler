package com.task.scheduler.core.service;

import com.task.scheduler.core.domain.Task;
import com.task.scheduler.core.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskProducerService {

    @Autowired
    TaskRepository taskRepository;

    private static final int BATCH_SIZE = 100;
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
        System.out.println("Publishing task to stream: " + streamData);
        return redisTemplate.opsForStream().add(record);
    }

    @Transactional
    public List<Task> lockAndMarkTasksQueued() {
        List<UUID> taskIds = taskRepository.fetchPendingTaskIdsForUpdate(BATCH_SIZE);
        if (taskIds.isEmpty())
            return List.of();
        taskRepository.markTasksAsQueued(taskIds);
        return taskRepository.findAllById(taskIds);
    }
}
