package com.task.scheduler.config;

import com.task.scheduler.core.domain.SystemVariables;
import com.task.scheduler.core.repository.SystemVariablesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemVariablesInitializer {

    private static final Logger log = LoggerFactory.getLogger(SystemVariablesInitializer.class);

    @Bean
    public CommandLineRunner initSystemVariables(SystemVariablesRepository repository) {
        return args -> {
            try {
                boolean exists = repository.findAll().stream()
                        .anyMatch(v -> "task_submission_threads".equals(v.getVariableName()));
                if (!exists) {
                    SystemVariables v = new SystemVariables();
                    v.setVariableName("task_submission_threads");
                    v.setBooleanValue(false);
                    repository.save(v);
                    log.info("Inserted default system variable 'task_submission_threads'");
                } else {
                    log.info("System variable 'task_submission_threads' already present");
                }
            } catch (Exception e) {
                log.warn("Failed to initialize system variables at startup: {}", e.getMessage());
            }
        };
    }
}
