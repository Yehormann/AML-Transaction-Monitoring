package com.aml.dto;

import com.aml.model.Alert;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String status,
        int riskScoreSnapshot,
        String analystNote,
        TransactionSummary transaction,
        String firedRules,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record TransactionSummary(
            UUID id,
            String senderAccount,
            String receiverCountry,
            double amount,
            String currency
    ) {}

    public static AlertResponse from(Alert a) {
        var tx = a.getTransaction();
        return new AlertResponse(
                a.getId(),
                a.getStatus(),
                a.getRiskScoreSnapshot(),
                a.getAnalystNote(),
                new TransactionSummary(
                        tx.getId(),
                        tx.getSenderAccount(),
                        tx.getReceiverCountry(),
                        tx.getAmount().doubleValue(),
                        tx.getCurrency()
                ),
                tx.getFiredRules(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
