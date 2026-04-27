package com.industrial.saude.controller;

import com.industrial.saude.dto.ApiResponse;
import com.industrial.saude.dto.ColaboradorDTO;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.service.ColaboradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
@RequiredArgsConstructor
public class ColaboradorController {

    private final ColaboradorService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ColaboradorDTO>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/ativos")
    public ResponseEntity<ApiResponse<List<ColaboradorDTO>>> findAtivos() {
        return ResponseEntity.ok(ApiResponse.success(service.findAtivos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ColaboradorDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<ApiResponse<ColaboradorDTO>> findByMatricula(@PathVariable String matricula) {
        return ResponseEntity.ok(ApiResponse.success(service.findByMatricula(matricula)));
    }

    @GetMapping("/setor/{setor}")
    public ResponseEntity<ApiResponse<List<ColaboradorDTO>>> findBySetor(@PathVariable Colaborador.Setor setor) {
        return ResponseEntity.ok(ApiResponse.success(service.findBySetor(setor)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO_TRABALHO', 'ENFERMEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ColaboradorDTO>> save(@Valid @RequestBody ColaboradorDTO dto, Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(ApiResponse.success("Colaborador criado com sucesso", service.save(dto, username)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO_TRABALHO', 'ENFERMEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ColaboradorDTO>> update(@PathVariable Long id, @Valid @RequestBody ColaboradorDTO dto, Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(ApiResponse.success("Colaborador atualizado com sucesso", service.update(id, dto, username)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        service.delete(id, username);
        return ResponseEntity.ok(ApiResponse.success("Colaborador excluído com sucesso", null));
    }
}