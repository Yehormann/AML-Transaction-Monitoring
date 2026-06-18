package com.aml.service;

import com.aml.dto.DashboardStatsResponse;
import com.aml.repository.AlertRepository;
import com.aml.repository.SarReportRepository;
import com.aml.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final SarReportRepository sarReportRepository;

    public DashboardService(TransactionRepository transactionRepository,
                            AlertRepository alertRepository,
                            SarReportRepository sarReportRepository) {
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.sarReportRepository = sarReportRepository;
    }

    public DashboardStatsResponse getStats() {
        long total = transactionRepository.count();
        long flagged = transactionRepository.countByStatus("FLAGGED");
        double pct = total == 0 ? 0.0 : Math.round((flagged * 100.0 / total) * 10) / 10.0;
        long open = alertRepository.countByStatus("OPEN");
        long dismissed = alertRepository.countByStatus("DISMISSED");
        long escalated = alertRepository.countByStatus("ESCALATED");
        long sars = sarReportRepository.count();
        return new DashboardStatsResponse(total, flagged, pct, open, dismissed, escalated, sars);
    }
}
