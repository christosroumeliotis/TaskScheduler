package com.task.scheduler.core.service;

import com.task.scheduler.core.domain.Task;
import com.task.scheduler.core.domain.TaskStatus;
import com.task.scheduler.core.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskExecutionService {

    @Autowired
    TaskRepository taskRepository;
    private static final Logger log = LoggerFactory.getLogger(TaskExecutionService.class);

    @Transactional
    public void processTaskExecution(Map<String, String> payloadMap, String workerId){
        UUID uuid = UUID.fromString(payloadMap.get("taskId"));
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Task with ID " + uuid + " not found"));

        task.setStatus(TaskStatus.RUNNING);
        task.setLockedAt(Instant.now());
        task.setLockedBy(workerId);
        taskRepository.save(task);

        try {
            executingBussinessLogic(task);
            
            task.setStatus(TaskStatus.COMPLETED);
            task.setLockedAt(null);
            task.setLockedBy(null);
            taskRepository.save(task);

            log.info("Task [{}] successfully executed", task.getId());
        } catch (Exception e) {
            handleTaskExecutionFailure(task, e);
        }
    }

    private void executingBussinessLogic(Task task) throws InterruptedException {
        log.info("Executing task {} with payload: {}", task.getId(), task.getPayload());
        Thread.sleep(500);
    }

    private void handleTaskExecutionFailure(Task task, Exception e) {
        log.error("Error executing task {}: {}", task.getId(), e.getMessage());
        int nextRetry = task.getCurrentRetry() + 1;
        task.setCurrentRetry(nextRetry);
        task.setLastError(e.getMessage());
        task.setLockedBy(null);

        if(nextRetry >= task.getMaxRetries()) {
            task.setStatus(TaskStatus.FAILED);
            log.error("Task [{}] has reached max retries and is marked as FAILED", task.getId());
        } else {
            long backoffSeconds = (long) Math.pow(2, nextRetry) * 5;
            task.setStatus(TaskStatus.PENDING);
            task.setExecuteAt(Instant.now().plusSeconds(backoffSeconds));
            log.warn("Task [{}] failed (attempt {}/{}). Scheduled retry in {}s.",
                    task.getId(), nextRetry, task.getMaxRetries(), backoffSeconds);
        }
        taskRepository.save(task);
    }
}
