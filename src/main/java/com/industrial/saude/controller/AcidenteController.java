package com.industrial.saude.controller;

import com.industrial.saude.dto.AcidenteTrabalhoDTO;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.service.AcidenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/acidentes")
@RequiredArgsConstructor
public class AcidenteController {
    
    private final AcidenteService service;
    
    @GetMapping
    public ResponseEntity<List<AcidenteTrabalhoDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/mes")
    public ResponseEntity<List<AcidenteTrabalhoDTO>> findMes() {
        return ResponseEntity.ok(service.findMes());
    }
    
    @GetMapping("/pendentes")
    public ResponseEntity<List<AcidenteTrabalhoDTO>> findPendentes() {
        return ResponseEntity.ok(service.findNaoEmitida());
    }
    
    @GetMapping("/colaborador/{colaboradorId}")
    public ResponseEntity<List<AcidenteTrabalhoDTO>> findByColaborador(@PathVariable Long colaboradorId) {
        return ResponseEntity.ok(service.findByColaborador(colaboradorId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AcidenteTrabalhoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<AcidenteTrabalhoDTO> save(@Valid @RequestBody AcidenteTrabalhoDTO dto, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.save(dto, usuario.getId()));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AcidenteTrabalhoDTO> update(@PathVariable Long id, @RequestBody AcidenteTrabalhoDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
    
    @PostMapping("/{id}/cat")
    public ResponseEntity<Void> emitirCat(@PathVariable Long id) {
        service.emitirCat(id);
        return ResponseEntity.ok().build();
    }
}