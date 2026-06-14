package com.aml.repository;

import com.aml.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findBySenderAccountAndTimestampAfter(String senderAccount, LocalDateTime after);

    List<Transaction> findBySenderAccountAndAmountAndTimestampAfter(String senderAccount, BigDecimal amount, LocalDateTime after);
}
