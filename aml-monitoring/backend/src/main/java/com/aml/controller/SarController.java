package com.aml.controller;

import com.aml.service.SarService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports/sar")
public class SarController {

    private final SarService sarService;

    public SarController(SarService sarService) {
        this.sarService = sarService;
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
