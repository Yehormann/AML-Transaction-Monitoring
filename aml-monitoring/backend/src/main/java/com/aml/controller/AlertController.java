package com.aml.controller;

import com.aml.dto.AlertResponse;
import com.aml.service.AlertService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> getAll(@RequestParam(required = false) String status) {
        return alertService.getAll(status);
    }

    @PatchMapping("/{id}/dismiss")
    public AlertResponse dismiss(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return alertService.dismiss(id, body.get("note"), auth.getName());
    }

    @PatchMapping("/{id}/escalate")
    public AlertResponse escalate(@PathVariable UUID id, Authentication auth) {
        return alertService.escalate(id, auth.getName());
    }
}
