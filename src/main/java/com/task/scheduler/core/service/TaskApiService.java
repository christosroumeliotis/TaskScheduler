package com.task.scheduler.core.service;

import com.task.scheduler.core.DTOs.TaskRequest;
import com.task.scheduler.core.DTOs.TaskResponse;
import com.task.scheduler.core.domain.Task;
import com.task.scheduler.core.domain.TaskStatus;
import com.task.scheduler.core.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskApiService {

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private TaskRepository taskRepository;

    private List<Thread> threadsRunning = new ArrayList<>();

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

    public Boolean beginThreads() {

        try {
            TaskSubmission taskSubmission = new TaskSubmission(this);


            for (int i=0; i<3; i++) {
                Thread thread = new Thread(taskSubmission);
                thread.start();
                System.out.println("Thread " + thread.getName() + " started running!");
                threadsRunning.add(thread);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Failed to start task submission threads!");
            return false;
        }
    }

    public void stopMultipleTaskSubmission() {
        for (Thread thread : threadsRunning) {
            thread.interrupt();
            System.out.println("Thread " + thread.getName() + " stopped running!");
        }

        for (Thread thread : threadsRunning) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        threadsRunning.clear();
    }
}

class TaskSubmission implements Runnable {


    private final TaskApiService taskApiService;
    private final AtomicInteger taskCounter = new AtomicInteger(0);

    public TaskSubmission(TaskApiService taskApiService) {
        this.taskApiService = taskApiService;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            TaskRequest request = TaskRequest.builder()
                    .uniqueKey("Task-" + Thread.currentThread().getName() + "-" + taskCounter.incrementAndGet())
                    .payload("{\"action\": \"SEND_INVOICE\", \"orderId\": 9999}")
                    .maxRetries(3)
                    .build();
            taskApiService.submitTask(request);
            System.out.println("Thread " + Thread.currentThread().getName() + " submitted a new task!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}