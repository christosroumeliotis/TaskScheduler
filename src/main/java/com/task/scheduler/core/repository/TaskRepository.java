package com.task.scheduler.core.repository;

import com.task.scheduler.core.domain.Task;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query(value = """
       SELECT BIN_TO_UUID(id) FROM tasks 
       WHERE status IN ('PENDING', 'FAILED') 
         AND execute_at <= NOW(3) 
         AND current_retry < max_retries 
       LIMIT :batchSize 
       FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<UUID> fetchPendingTaskIdsForUpdate(@Param("batchSize") int batchSize);

    @Modifying
    @Query("""
    UPDATE Task t
    SET t.status = com.task.scheduler.core.domain.TaskStatus.QUEUED, 
        t.updatedAt = CURRENT_TIMESTAMP
    WHERE t.id IN (:ids)
    """)
    void markTasksAsQueued(@Param("ids") List<UUID> ids);

    Optional<Task> findByUniqueKey(@NotBlank(message = "Unique key is required") String s);
}
