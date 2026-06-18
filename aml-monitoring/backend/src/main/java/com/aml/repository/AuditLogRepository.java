package com.aml.repository;

import com.aml.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityIdOrderByTimestampAsc(UUID entityId);

    List<AuditLog> findAllByOrderByTimestampDesc();
}
