package com.task.scheduler.core.service;

import com.task.scheduler.core.DTOs.TaskRequest;
import com.task.scheduler.core.DTOs.TaskResponse;
import com.task.scheduler.core.domain.Task;
import com.task.scheduler.core.domain.TaskStatus;
import com.task.scheduler.core.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TaskApiService {

    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public TaskResponse submitTask(TaskRequest request) {

        Optional<Task> existingTask = taskRepository.findByUniqueKey(request.uniqueKey());
        if (existingTask.isPresent()) {
            return mapToResponse(existingTask.get());
        }

        Task task = new Task();
        task.setUniqueKey(request.uniqueKey());
        task.setQueueName(request.queueName());
        task.setPayload(request.payload());
        task.setPriority(request.priority());
        task.setMaxRetries(request.maxRetries());
        task.setExecuteAt(request.executeAt());
        task.setStatus(TaskStatus.PENDING);

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getUniqueKey(),
                task.getQueueName(),
                task.getStatus(),
                task.getPriority(),
                task.getCurrentRetry(),
                task.getMaxRetries(),
                task.getPayload(),
                task.getLastError(),
                task.getExecuteAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
