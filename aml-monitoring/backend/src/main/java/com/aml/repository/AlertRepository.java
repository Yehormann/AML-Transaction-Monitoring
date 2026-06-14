package com.aml.repository;

import com.aml.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByStatus(String status);

    List<Alert> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}
