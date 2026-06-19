package com.aml.config;

import com.aml.dto.TransactionRequest;
import com.aml.repository.TransactionRepository;
import com.aml.service.TransactionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public DataSeeder(TransactionService transactionService, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        if (transactionRepository.count() > 0) {
            return; // already seeded
        }

        LocalDateTime now = LocalDateTime.now();

        // Clean transactions (APPROVED — low risk)
        submit("ACC-2210", "DE", "ACC-3301", "DE", "2025-06-01", 1200, "EUR", now.minusDays(5));
        submit("ACC-4450", "NL", "ACC-5512", "BE", "2025-09-15", 3400, "EUR", now.minusDays(4).minusHours(3));
        submit("ACC-0091", "FR", "ACC-1120", "DE", "2026-01-10", 750, "EUR", now.minusDays(3).minusHours(6));
        submit("ACC-8812", "LU", "ACC-9904", "NL", "2025-11-20", 5000, "EUR", now.minusDays(2).minusHours(1));
        submit("ACC-6634", "AT", "ACC-7721", "CH", "2026-03-05", 2800, "EUR", now.minusDays(1).minusHours(4));

        // Large amount — triggers LargeAmountRule (+40), flagged
        submit("ACC-1192", "LU", "ACC-4487", "LU", "2025-08-12", 14500, "EUR", now.minusDays(3));
        submit("ACC-0033", "DE", "ACC-2299", "FR", "2026-02-20", 25000, "EUR", now.minusDays(2).minusHours(5));

        // High-risk country — triggers HighRiskCountryRule (+35) + LargeAmountRule (+40) = 75, auto-escalated + SAR
        submit("ACC-7743", "DE", "ACC-8890", "IR", "2025-05-01", 50000, "EUR", now.minusDays(1));
        submit("ACC-3310", "NL", "ACC-4401", "KP", "2024-12-01", 18000, "USD", now.minusDays(1).minusHours(2));

        // Dormant account — triggers DormantAccountRule (+25), combined with large amount (+40) = 65
        submit("ACC-5501", "FR", "ACC-6602", "US", "2020-01-15", 15000, "EUR", now.minusHours(12));

        // High risk country only (+35) — below alert threshold
        submit("ACC-2205", "BE", "ACC-3309", "RU", "2025-07-10", 4500, "EUR", now.minusHours(6));

        // Dormant + high risk country = 25+35 = 60, flagged
        submit("ACC-9901", "DE", "ACC-0012", "SY", "2019-06-01", 8000, "EUR", now.minusHours(3));

        // Large + dormant = 40+25 = 65, flagged
        submit("ACC-1100", "AT", "ACC-2200", "US", "2021-03-10", 12000, "EUR", now.minusHours(1));

        // Large + high-risk + dormant = 40+35+25 = 100, auto-escalated + SAR
        submit("ACC-4477", "CH", "ACC-5588", "BY", "2018-11-01", 75000, "EUR", now.minusMinutes(30));
    }

    private void submit(String senderAcc, String senderCountry, String receiverAcc, String receiverCountry,
                         String lastActive, double amount, String currency, LocalDateTime timestamp) {
        TransactionRequest req = new TransactionRequest(
                senderAcc, senderCountry, receiverAcc, receiverCountry,
                LocalDate.parse(lastActive),
                BigDecimal.valueOf(amount),
                currency, timestamp
        );
        transactionService.submit(req, "SYSTEM");
    }
}
