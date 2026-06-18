package com.aml.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sender_account", nullable = false)
    private String senderAccount;

    @Column(name = "sender_country", nullable = false)
    private String senderCountry;

    @Column(name = "receiver_account", nullable = false)
    private String receiverAccount;

    @Column(name = "receiver_country", nullable = false)
    private String receiverCountry;

    @Column(name = "receiver_last_active", nullable = false)
    private LocalDate receiverLastActive;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "EUR";

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "risk_score", nullable = false)
    private int riskScore = 0;

    @Column(nullable = false)
    private String status = "APPROVED";

    @Column(name = "fired_rules", nullable = false, length = 5000)
    private String firedRules = "[]";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }
    public String getSenderCountry() { return senderCountry; }
    public void setSenderCountry(String senderCountry) { this.senderCountry = senderCountry; }
    public String getReceiverAccount() { return receiverAccount; }
    public void setReceiverAccount(String receiverAccount) { this.receiverAccount = receiverAccount; }
    public String getReceiverCountry() { return receiverCountry; }
    public void setReceiverCountry(String receiverCountry) { this.receiverCountry = receiverCountry; }
    public LocalDate getReceiverLastActive() { return receiverLastActive; }
    public void setReceiverLastActive(LocalDate receiverLastActive) { this.receiverLastActive = receiverLastActive; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFiredRules() { return firedRules; }
    public void setFiredRules(String firedRules) { this.firedRules = firedRules; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
