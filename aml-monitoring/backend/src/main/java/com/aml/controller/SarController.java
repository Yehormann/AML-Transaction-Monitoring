package com.aml.controller;

import com.aml.dto.SarReportResponse;
import com.aml.repository.SarReportRepository;
import com.aml.service.SarService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/sar")
public class SarController {

    private final SarService sarService;
    private final SarReportRepository sarReportRepository;

    public SarController(SarService sarService, SarReportRepository sarReportRepository) {
        this.sarService = sarService;
        this.sarReportRepository = sarReportRepository;
    }

    @GetMapping
    public List<SarReportResponse> getAll() {
        return sarReportRepository.findAllByOrderByFiledAtDesc()
                .stream().map(SarReportResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        byte[] pdf = sarService.getReportPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"SAR-" + id + ".pdf\"");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
