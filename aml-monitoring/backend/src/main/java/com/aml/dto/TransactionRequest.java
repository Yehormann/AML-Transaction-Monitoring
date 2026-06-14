package com.aml.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionRequest(
        @NotBlank String senderAccount,
        @NotBlank String senderCountry,
        @NotBlank String receiverAccount,
        @NotBlank String receiverCountry,
        @NotNull  LocalDate receiverLastActive,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotNull  LocalDateTime timestamp
) {}
