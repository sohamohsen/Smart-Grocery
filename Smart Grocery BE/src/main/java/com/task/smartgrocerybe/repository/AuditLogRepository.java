package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.AuditLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogs, Integer> {
}
