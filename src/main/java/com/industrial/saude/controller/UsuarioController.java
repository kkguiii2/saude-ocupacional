package com.industrial.saude.controller;

import com.industrial.saude.dto.UsuarioRequest;
import com.industrial.saude.dto.UsuarioResponse;
import com.industrial.saude.service.UsuarioService;
import com.industrial.saude.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtTokenProvider tokenProvider;

    /** GET /api/usuarios — lista todos os usuários (apenas ADMINISTRADOR) */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(HttpServletRequest request) {
        verificarAdmin(request);
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /** GET /api/usuarios/{id} — busca por ID (apenas ADMINISTRADOR) */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id, HttpServletRequest request) {
        verificarAdmin(request);
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /** POST /api/usuarios — cria novo usuário (apenas ADMINISTRADOR) */
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest req,
                                                  HttpServletRequest request) {
        verificarAdmin(request);
        String operador = extrairUsername(request);
        UsuarioResponse resp = usuarioService.criar(req, operador);
        log.info("Usuário criado via API: {} por {}", resp.getUsername(), operador);
        return ResponseEntity.status(201).body(resp);
    }

    /** PUT /api/usuarios/{id} — atualiza usuário (apenas ADMINISTRADOR) */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequest req,
                                                      HttpServletRequest request) {
        verificarAdmin(request);
        String operador = extrairUsername(request);
        return ResponseEntity.ok(usuarioService.atualizar(id, req, operador));
    }

    /** PATCH /api/usuarios/{id}/status — ativa ou desativa (apenas ADMINISTRADOR) */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UsuarioResponse> alterarStatus(@PathVariable Long id,
                                                          @RequestParam boolean ativo,
                                                          HttpServletRequest request) {
        verificarAdmin(request);
        String operador = extrairUsername(request);
        return ResponseEntity.ok(usuarioService.alterarStatus(id, ativo, operador));
    }

    /** PATCH /api/usuarios/{id}/senha — redefine senha (apenas ADMINISTRADOR) */
    @PatchMapping("/{id}/senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@PathVariable Long id,
                                                               @RequestBody Map<String, String> body,
                                                               HttpServletRequest request) {
        verificarAdmin(request);
        String operador = extrairUsername(request);
        String novaSenha = body.get("novaSenha");
        usuarioService.redefinirSenha(id, novaSenha, operador);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String extrairUsername(HttpServletRequest request) {
        String token = extrairToken(request);
        if (token == null) return "desconhecido";
        return tokenProvider.getUsernameFromToken(token);
    }

    private void verificarAdmin(HttpServletRequest request) {
        String token = extrairToken(request);
        if (token == null || !tokenProvider.validateToken(token)) {
            throw new SecurityException("Acesso não autorizado");
        }
        String perfil = tokenProvider.getRoleFromToken(token);
        if (!"ADMINISTRADOR".equals(perfil)) {
            throw new SecurityException("Apenas administradores podem gerenciar usuários");
        }
    }
}
