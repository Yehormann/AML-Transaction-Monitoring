package com.aml.service;

import com.aml.dto.TransactionRequest;
import com.aml.dto.TransactionResponse;
import com.aml.engine.RuleEngineService;
import com.aml.engine.RuleResult;
import com.aml.model.Alert;
import com.aml.model.Transaction;
import com.aml.repository.AlertRepository;
import com.aml.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    private static final int ALERT_THRESHOLD = 40;
    private static final int AUTO_ESCALATE_THRESHOLD = 75;

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final RuleEngineService ruleEngineService;
    private final SarService sarService;
    private final ObjectMapper objectMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            AlertRepository alertRepository,
            RuleEngineService ruleEngineService,
            SarService sarService,
            ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.ruleEngineService = ruleEngineService;
        this.sarService = sarService;
        this.objectMapper = objectMapper;
    }

    public TransactionResponse submit(TransactionRequest req, String performedBy) {
        Transaction tx = new Transaction();
        tx.setSenderAccount(req.senderAccount());
        tx.setSenderCountry(req.senderCountry());
        tx.setReceiverAccount(req.receiverAccount());
        tx.setReceiverCountry(req.receiverCountry());
        tx.setReceiverLastActive(req.receiverLastActive());
        tx.setAmount(req.amount());
        tx.setCurrency(req.currency());
        tx.setTimestamp(req.timestamp());

        List<RuleResult> fired = ruleEngineService.evaluate(tx);
        int score = ruleEngineService.totalScore(fired);

        tx.setRiskScore(score);
        tx.setFiredRules(toJson(fired));
        if (score > ALERT_THRESHOLD) {
            tx.setStatus("FLAGGED");
        }
        tx = transactionRepository.save(tx);

        if (score > ALERT_THRESHOLD) {
            Alert alert = new Alert();
            alert.setTransaction(tx);
            alert.setRiskScoreSnapshot(score);
            alert = alertRepository.save(alert);

            if (score > AUTO_ESCALATE_THRESHOLD) {
                alert.setStatus("ESCALATED");
                alert.setUpdatedAt(LocalDateTime.now());
                alertRepository.save(alert);
                sarService.fileReport(alert, "SYSTEM");
            }
        }

        return TransactionResponse.from(tx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAll() {
        return transactionRepository.findAll().stream()
                .map(TransactionResponse::from)
                .toList();
    }

    private String toJson(List<RuleResult> rules) {
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
