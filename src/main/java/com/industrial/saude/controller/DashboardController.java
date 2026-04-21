package com.industrial.saude.controller;

import com.industrial.saude.dto.DashboardDTO;
import com.industrial.saude.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService service;
    
    @GetMapping
    public ResponseEntity<DashboardDTO> getDados() {
        return ResponseEntity.ok(service.getDados());
    }
}