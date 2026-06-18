package com.aml.dto;

import com.aml.model.AuditLog;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String entityType,
        UUID entityId,
        String action,
        String performedBy,
        String note,
        LocalDateTime timestamp
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(), log.getEntityType(), log.getEntityId(),
                log.getAction(), log.getPerformedBy(), log.getNote(), log.getTimestamp()
        );
    }
}
