package com.aml.service;

import com.aml.dto.AlertResponse;
import com.aml.model.Alert;
import com.aml.model.AuditLog;
import com.aml.repository.AlertRepository;
import com.aml.repository.AuditLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final AuditLogRepository auditLogRepository;
    private final SarService sarService;

    public AlertService(AlertRepository alertRepository, AuditLogRepository auditLogRepository, SarService sarService) {
        this.alertRepository = alertRepository;
        this.auditLogRepository = auditLogRepository;
        this.sarService = sarService;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAll(String status) {
        List<Alert> alerts = (status != null && !status.isBlank())
                ? alertRepository.findByStatus(status.toUpperCase())
                : alertRepository.findAllByOrderByCreatedAtDesc();
        return alerts.stream().map(AlertResponse::from).toList();
    }

    public AlertResponse dismiss(UUID id, String note, String performedBy) {
        if (note == null || note.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Analyst note is required to dismiss an alert");
        }
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));

        alert.setStatus("DISMISSED");
        alert.setAnalystNote(note);
        alert.setUpdatedAt(LocalDateTime.now());
        alertRepository.save(alert);

        auditLogRepository.save(new AuditLog("ALERT", id, "ALERT_DISMISSED", performedBy, note));

        return AlertResponse.from(alert);
    }

    public AlertResponse escalate(UUID id, String performedBy) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));

        if ("DISMISSED".equals(alert.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot escalate a dismissed alert");
        }

        alert.setStatus("ESCALATED");
        alert.setUpdatedAt(LocalDateTime.now());
        alertRepository.save(alert);

        sarService.fileReport(alert, performedBy);

        auditLogRepository.save(new AuditLog("ALERT", id, "ALERT_ESCALATED", performedBy, null));

        return AlertResponse.from(alert);
    }
}
