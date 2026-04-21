package com.industrial.saude.controller;

import com.industrial.saude.dto.ApiResponse;
import com.industrial.saude.dto.AuditoriaDTO;
import com.industrial.saude.model.AuditoriaLog;
import com.industrial.saude.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<AuditoriaDTO>>> buscarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditoriaLog> logs;

        if (modulo != null || usuarioId != null || acao != null || inicio != null || fim != null) {
            List<AuditoriaLog> filtered = service.buscarPorFiltros(modulo, usuarioId, acao, inicio, fim);
            logs = filtered.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                        int start = (int) pageable.getOffset();
                        int end = Math.min(start + pageable.getPageSize(), list.size());
                        return list.subList(start, end).isEmpty() ? Page.empty() : Page.empty();
                    }));
            if (logs.isEmpty() && !filtered.isEmpty()) {
                logs = Page.empty();
            }
        } else {
            logs = service.buscarTodos(pageable);
        }

        List<AuditoriaDTO> dtos = logs.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        ApiResponse<List<AuditoriaDTO>> response = new ApiResponse<>();
        response.setData(dtos);
        response.setMessage("Logs de auditoria carregados");
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/modulo/{modulo}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<AuditoriaDTO>>> buscarPorModulo(@PathVariable String modulo) {
        List<AuditoriaLog> logs = service.buscarPorModulo(modulo);

        List<AuditoriaDTO> dtos = logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        ApiResponse<List<AuditoriaDTO>> response = new ApiResponse<>();
        response.setData(dtos);
        response.setMessage("Logs do módulo " + modulo);
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<AuditoriaDTO>>> buscarPorUsuario(@PathVariable Long usuarioId) {
        List<AuditoriaLog> logs = service.buscarPorUsuario(usuarioId);

        List<AuditoriaDTO> dtos = logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        ApiResponse<List<AuditoriaDTO>> response = new ApiResponse<>();
        response.setData(dtos);
        response.setMessage("Logs do usuário carregados");
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    private AuditoriaDTO toDTO(AuditoriaLog entity) {
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setId(entity.getId());
        dto.setAcao(entity.getAcao());
        dto.setModulo(entity.getModulo());
        dto.setDescricao(entity.getDescricao());
        dto.setDetalhes(entity.getDetalhes());
        dto.setIp(entity.getIp());
        dto.setDataHora(entity.getDataHora());

        if (entity.getUsuario() != null) {
            dto.setUsername(entity.getUsuario().getUsername());
        }

        return dto;
    }
}