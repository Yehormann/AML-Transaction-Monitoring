package com.aml.dto;

import com.aml.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String senderAccount,
        String senderCountry,
        String receiverAccount,
        String receiverCountry,
        LocalDate receiverLastActive,
        BigDecimal amount,
        String currency,
        LocalDateTime timestamp,
        int riskScore,
        String status,
        String firedRules,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getSenderAccount(), t.getSenderCountry(),
                t.getReceiverAccount(), t.getReceiverCountry(), t.getReceiverLastActive(),
                t.getAmount(), t.getCurrency(), t.getTimestamp(),
                t.getRiskScore(), t.getStatus(), t.getFiredRules(), t.getCreatedAt()
        );
    }
}
