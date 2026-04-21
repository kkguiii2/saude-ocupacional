package com.industrial.saude.security;

import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();
        String authHeader = request.getHeader("Authorization");

        log.debug("[JWT] {} {} | IP: {} | Auth: {}", method, path, remoteAddr,
            authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 30)) + "..." : "AUSENTE");

        // Se não tem header Authorization, deixa passar (será interceptado depois pelo SecurityConfig)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("[JWT] {} {} | Sem Bearer token — passando adiante", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        try {
            token = authHeader.substring(7);
        } catch (Exception e) {
            log.warn("[JWT] Token malformado em {} {}: {}", method, path, e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Valida token
        if (!tokenProvider.validateToken(token)) {
            log.warn("[JWT] Token INVÁLIDO/EXPIRADO em {} {} | IP: {}", method, path, remoteAddr);
            sendUnauthorized(response, "Token inválido ou expirado");
            return;
        }

        String username;
        try {
            username = tokenProvider.getUsernameFromToken(token);
        } catch (Exception e) {
            log.warn("[JWT] Erro ao extrair username em {} {}: {}", method, path, e.getMessage());
            sendUnauthorized(response, "Token inválido");
            return;
        }

        // Busca usuário
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        if (usuario == null) {
            log.warn("[JWT] Usuário '{}' não encontrado no banco | {} {}", username, method, path);
            sendUnauthorized(response, "Usuário não encontrado");
            return;
        }

        if (!usuario.isAtivo()) {
            log.warn("[JWT] Usuário '{}' inativo | {} {}", username, method, path);
            sendUnauthorized(response, "Usuário inativo");
            return;
        }

        // Define autenticação no contexto
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name())
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("[JWT] ✓ Autenticado: {} | Perfil: {} | {} {} | IP: {}",
            username, usuario.getPerfil(), method, path, remoteAddr);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // O filtro JWT SÓ deve agir em rotas de API (/api/**)
        // Rotas de autenticação são sempre públicas
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // Rotas de página Thymeleaf são públicas — o JS verifica o token no frontend
        // Apenas /api/** precisa de validação JWT no backend
        if (!path.startsWith("/api/")) {
            return true;
        }

        // Para /api/**: executa o filtro (valida JWT)
        return false;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}