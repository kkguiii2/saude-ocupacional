package com.industrial.saude.controller;

import com.industrial.saude.dto.MedicamentoDTO;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {
    
    private final EstoqueService service;
    
    @GetMapping
    public ResponseEntity<List<MedicamentoDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/baixo")
    public ResponseEntity<List<MedicamentoDTO>> findEstoqueBaixo() {
        return ResponseEntity.ok(service.findEstoqueBaixo());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<MedicamentoDTO> save(@Valid @RequestBody MedicamentoDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }
    
    @PostMapping("/{id}/entrada")
    public ResponseEntity<MedicamentoDTO> entrada(@PathVariable Long id, @RequestParam int quantidade, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.entrada(id, quantidade, usuario.getId()));
    }
    
    @PostMapping("/{id}/saida")
    public ResponseEntity<MedicamentoDTO> saida(@PathVariable Long id, @RequestParam int quantidade, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.saida(id, quantidade, usuario.getId()));
    }
}