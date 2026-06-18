package com.aml.repository;

import com.aml.model.SarReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SarReportRepository extends JpaRepository<SarReport, UUID> {

    List<SarReport> findAllByOrderByFiledAtDesc();
}
