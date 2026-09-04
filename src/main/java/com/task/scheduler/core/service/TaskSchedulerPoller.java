package com.task.scheduler.core.service;

import com.task.scheduler.core.domain.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableScheduling
public class TaskSchedulerPoller {

    private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerPoller.class);
    private final TaskProducerService taskProducerService;

    public TaskSchedulerPoller(TaskProducerService taskProducerService) {
        this.taskProducerService = taskProducerService;
    }

    @Scheduled(fixedRate = 1000)
    //@Transactional
    public void pollAndEnqueueTasks() {
        List<Task> tasksRetrieved = taskProducerService.lockAndMarkTasksQueued();
        if (tasksRetrieved.isEmpty()) {
            logger.info("No tasks found for execution at this time.");
            return;
        }
        logger.info("Retrieved and enqueued {} tasks", tasksRetrieved.size());

        for (Task task : tasksRetrieved) {
            try {
                taskProducerService.publishToStream(task);
            } catch (Exception e) {
                logger.error("Error while publishing task {} to stream: {}", task.getId(), e.getMessage());
            }
        }
    }
}
