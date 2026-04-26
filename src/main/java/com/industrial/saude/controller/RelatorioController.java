package com.industrial.saude.controller;

import com.industrial.saude.model.Usuario;
import com.industrial.saude.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/atendimento/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarPdfAtendimento(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] pdf = relatorioService.gerarPdfAtendimento(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "atendimento_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Erro ao gerar PDF do atendimento: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/colaborador/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarPdfHistoricoColaborador(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] pdf = relatorioService.gerarPdfHistoricoColaborador(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "historico_colaborador_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Erro ao gerar PDF do histórico do colaborador: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/colaboradores/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarExcelColaboradores(
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] excel = relatorioService.gerarExcelColaboradores();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "colaboradores.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (Exception e) {
            log.error("Erro ao gerar Excel de colaboradores: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/atendimentos/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarExcelAtendimentos(
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] excel = relatorioService.gerarExcelAtendimentos();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "atendimentos.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (Exception e) {
            log.error("Erro ao gerar Excel de atendimentos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/agendamentos/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarExcelAgendamentos(
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] excel = relatorioService.gerarExcelAgendamentos();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "agendamentos.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (Exception e) {
            log.error("Erro ao gerar Excel de agendamentos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estoque/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarExcelEstoque(
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] excel = relatorioService.gerarExcelEstoque();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "estoque.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (Exception e) {
            log.error("Erro ao generar Excel de estoque: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/acidentes/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarExcelAcidentes(
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] excel = relatorioService.gerarExcelAcidentes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "acidentes.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (Exception e) {
            log.error("Erro ao gerar Excel de acidentes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/completo/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarExcelCompleto(
            @RequestBody List<String> modulos,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            if (modulos == null || modulos.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            byte[] excel = relatorioService.gerarExcelCompleto(modulos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "relatorio_completo.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (Exception e) {
            log.error("Erro ao gerar Excel completo: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/nr7/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> gerarExcelNR7(
            @AuthenticationPrincipal Usuario usuario) {
        try {
            byte[] excel = relatorioService.gerarExcelNR7();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "relatorio_pcmso_nr7.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (Exception e) {
            log.error("Erro ao gerar Excel NR-7: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}