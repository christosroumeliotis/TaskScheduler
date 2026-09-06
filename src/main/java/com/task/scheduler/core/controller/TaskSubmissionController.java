package com.task.scheduler.core.controller;

import com.task.scheduler.core.DTOs.TaskRequest;
import com.task.scheduler.core.DTOs.TaskResponse;
import com.task.scheduler.core.service.TaskApiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
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

        Boolean startedSuccessfully = taskApiService.beginThreads();
        if(startedSuccessfully)
            return ResponseEntity.status(HttpStatus.OK).body("Threads started!");
         else
            return ResponseEntity.internalServerError().body("Threads did not start!");
    }

    @PutMapping("/submit/multiple")
    public ResponseEntity<String> stopMultipleTask() {
        taskApiService.stopMultipleTaskSubmission();
        return ResponseEntity.status(HttpStatus.OK).body("Threads stopped!");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}


