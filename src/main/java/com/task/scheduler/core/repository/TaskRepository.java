package com.task.scheduler.core.repository;

import com.task.scheduler.core.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query(value = """
        SELECT id, 
        FROM tasks
        WHERE status IN ('PENDING', 'FAILED')
          AND execute_at <= NOW(3)
          AND current_retry < max_retries
        ORDER BY priority DESC, execute_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<String> fetchPendingTaskIdsForUpdate(@Param("batchSize") int batchSize);

    @Modifying
    @Query(value = """
        UPDATE tasks
        SET status = 'QUEUED', updated_at = NOW(3)
        WHERE id IN (:ids)
        """, nativeQuery = true)
    void markTasksAsQueued(@Param("ids") List<String> ids);
}
