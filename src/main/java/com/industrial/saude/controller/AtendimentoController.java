package com.industrial.saude.controller;

import com.industrial.saude.dto.AtendimentoDTO;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.service.AtendimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {
    
    private final AtendimentoService service;
    
    @GetMapping
    public ResponseEntity<List<AtendimentoDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/hoje")
    public ResponseEntity<List<AtendimentoDTO>> findHoje() {
        return ResponseEntity.ok(service.findHoje());
    }
    
    @GetMapping("/emergencias")
    public ResponseEntity<List<AtendimentoDTO>> findEmergencias() {
        return ResponseEntity.ok(service.findEmergencias());
    }
    
    @GetMapping("/colaborador/{colaboradorId}")
    public ResponseEntity<List<AtendimentoDTO>> findByColaborador(@PathVariable Long colaboradorId) {
        return ResponseEntity.ok(service.findByColaborador(colaboradorId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AtendimentoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<AtendimentoDTO> save(@Valid @RequestBody AtendimentoDTO dto, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.save(dto, usuario.getId()));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AtendimentoDTO> update(@PathVariable Long id, @RequestBody AtendimentoDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}