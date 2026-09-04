package com.task.scheduler.core.repository;

import com.task.scheduler.core.domain.SystemVariables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemVariablesRepository extends JpaRepository<SystemVariables, Long> {
}
