package com.aml.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aml.dto.AuditLogResponse;
import com.aml.repository.AuditLogRepository;

@RestController
@RequestMapping("/api/audit-log")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public List<AuditLogResponse> getAll(@RequestParam(required = false) UUID entityId) {
        if (entityId != null) {
            return auditLogRepository.findByEntityIdOrderByTimestampAsc(entityId).stream().map(AuditLogResponse::from).toList();
        }
        return auditLogRepository.findAllByOrderByTimestampDesc().stream().map(AuditLogResponse::from).toList();
    }
}
