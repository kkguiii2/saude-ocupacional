package com.industrial.saude.controller;

import com.industrial.saude.dto.AlertaDTO;
import com.industrial.saude.dto.ApiResponse;
import com.industrial.saude.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService service;

    @GetMapping
    public ResponseEntity<ApiResponse<AlertaDTO>> getAlertas() {
        return ResponseEntity.ok(ApiResponse.success(service.getAlertas()));
    }
}