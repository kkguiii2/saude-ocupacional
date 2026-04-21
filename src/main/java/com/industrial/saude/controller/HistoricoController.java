package com.industrial.saude.controller;

import com.industrial.saude.dto.ApiResponse;
import com.industrial.saude.dto.HistoricoColaboradorDTO;
import com.industrial.saude.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService service;

    @GetMapping("/colaborador/{id}")
    public ResponseEntity<ApiResponse<HistoricoColaboradorDTO>> getHistoricoCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getHistoricoCompleto(id)));
    }

    @GetMapping("/exames-vencidos")
    public ResponseEntity<ApiResponse<List<HistoricoColaboradorDTO.ExameVencidoDTO>>> getExamesVencidos() {
        return ResponseEntity.ok(ApiResponse.success(service.getExamesVencidosGlobal()));
    }
}