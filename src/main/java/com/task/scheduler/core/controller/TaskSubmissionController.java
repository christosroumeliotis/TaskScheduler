package com.task.scheduler.core.controller;

import com.task.scheduler.core.DTOs.TaskRequest;
import com.task.scheduler.core.DTOs.TaskResponse;
import com.task.scheduler.core.service.TaskApiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/task")
public class TaskSubmissionController {

    @Autowired
    TaskApiService taskApiService;

    @PostMapping("/submit")
    public ResponseEntity<TaskResponse> submitTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskApiService.submitTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/submit/multiple")
    public ResponseEntity<String> submitMultipleTask() {

        TaskSubmission taskSubmission = new TaskSubmission(taskApiService);

        Thread thread1 = new Thread(taskSubmission);
        Thread thread2 = new Thread(taskSubmission);
        Thread thread3 = new Thread(taskSubmission);

        thread1.start();
        thread2.start();
        thread3.start();

//        try {
//            thread1.join();
//            thread2.join();
//            thread3.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return ResponseEntity.status(HttpStatus.OK).body("Threads started!");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
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
        while (true) {
            TaskRequest request = TaskRequest.builder()
                    .uniqueKey("Task-" + Thread.currentThread().getName() + "-" + taskCounter.incrementAndGet())
                    .payload("{\"action\": \"SEND_INVOICE\", \"orderId\": 9999}")
                    .maxRetries(3)
                    .build();
            taskApiService.submitTask(request);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
