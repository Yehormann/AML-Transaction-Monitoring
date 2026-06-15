package com.aml.dto;

import com.aml.model.SarReport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SarReportResponse(
        UUID id,
        UUID alertId,
        UUID transactionId,
        BigDecimal amount,
        String currency,
        LocalDateTime filedAt
) {
    public static SarReportResponse from(SarReport sar) {
        var tx = sar.getAlert().getTransaction();
        return new SarReportResponse(
                sar.getId(),
                sar.getAlert().getId(),
                tx.getId(),
                tx.getAmount(),
                tx.getCurrency(),
                sar.getFiledAt()
        );
    }
}
