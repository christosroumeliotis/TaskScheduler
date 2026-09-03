package com.task.scheduler.core.service;

import com.task.scheduler.core.domain.Task;
import com.task.scheduler.core.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskSchedulerPoller {

    @Autowired
    TaskRepository taskRepository;

    private static final int BATCH_SIZE = 100;
    private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerPoller.class);
    private final TaskProducerService taskProducerService;

    public TaskSchedulerPoller(TaskProducerService taskProducerService) {
        this.taskProducerService = taskProducerService;
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void pollAndEnqueueTasks() {
        List<String> tasksIdsRetrieved = taskRepository.fetchPendingTaskIdsForUpdate(BATCH_SIZE);
        if (tasksIdsRetrieved.isEmpty())
            return;
        logger.info("Retrieved {} tasks for enqueuing", tasksIdsRetrieved.size());

        List<Task> tasksToBeQueued = new ArrayList<>();
        for (String taskId : tasksIdsRetrieved) {
            Optional<Task> taskRetrieved = taskRepository.findById(UUID.fromString(taskId));
            taskRetrieved.ifPresent(tasksToBeQueued::add);
        }

        List<Task> tasksQueued = new ArrayList<>();
        for (Task task : tasksToBeQueued) {
            try {
                taskProducerService.publishToStream(task);
                tasksQueued.add(task);
            } catch (Exception e) {
                logger.error("Error while publishing task {} to stream: {}", task.getId(), e.getMessage());
            }
        }
        taskRepository.markTasksAsQueued(tasksQueued.stream()
                .map(m -> m.getId().toString())
                .toList());
    }
}
