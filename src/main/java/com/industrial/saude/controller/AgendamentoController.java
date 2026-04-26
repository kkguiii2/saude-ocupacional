package com.industrial.saude.controller;

import com.industrial.saude.dto.AgendamentoDTO;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {
    
    private final AgendamentoService service;
    
    @GetMapping
    public ResponseEntity<Page<AgendamentoDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/pendentes")
    public ResponseEntity<List<AgendamentoDTO>> findPendentes() {
        return ResponseEntity.ok(service.findPendentes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<AgendamentoDTO> save(@Valid @RequestBody AgendamentoDTO dto, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.save(dto, usuario.getId()));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> update(@PathVariable Long id, @RequestBody AgendamentoDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
    
    @PostMapping("/{id}/realizar")
    public ResponseEntity<AgendamentoDTO> realizar(@PathVariable Long id) {
        return ResponseEntity.ok(service.realizar(id));
    }
    
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}