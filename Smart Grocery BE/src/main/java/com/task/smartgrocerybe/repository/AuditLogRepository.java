package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.AuditLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogs, Integer> {
    Page<AuditLogs> findByPerformedBy(String performedBy, Pageable pageable);
}
