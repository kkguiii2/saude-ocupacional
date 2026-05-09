package com.industrial.saude.service;

import com.industrial.saude.dto.LoginRequest;
import com.industrial.saude.dto.LoginResponse;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.UsuarioRepository;
import com.industrial.saude.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditoriaService auditoriaService;

    @Value("${app.admin.password:Admin@Dev2026}")
    private String adminPassword;

    private final Map<String, AttemptTracker> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 30;

    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        String username = request.getUsername();
        log.info("Tentativa de login para usuário: {} - IP: {}", username, ip);

        if (isBlocked(username)) {
            log.warn("Login bloqueado para usuário: {} - IP: {}", username, ip);
            throw new SecurityException("Conta temporariamente bloqueada. Tente novamente em 30 minutos.");
        }

        try {
            Usuario usuario = repository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Usuário não encontrado no banco: {}", username);
                        return new RuntimeException("Usuário não encontrado");
                    });

            log.debug("Usuário encontrado: {} - Ativo: {} - Perfil: {}", 
                usuario.getUsername(), usuario.isAtivo(), usuario.getPerfil());

            if (usuario.isBloqueado()) {
                log.warn("Usuário bloqueado: {}", username);
                throw new SecurityException("Conta bloqueada. Entre em contato com o administrador.");
            }

            log.debug("Verificando senha...");
            boolean senhaValida = encoder.matches(request.getPassword(), usuario.getPassword());
            log.debug("Resultado comparação senha: {}", senhaValida);
            
            if (!senhaValida) {
                log.warn("Senha inválida para usuário: {} - IP: {}", username, ip);
                handleFailedLogin(username, ip);
                throw new RuntimeException("Credenciais inválidas");
            }

            if (!usuario.isAtivo()) {
                log.warn("Usuário inativo: {}", username);
                throw new RuntimeException("Usuário inativo");
            }

            usuario.setUltimoAcesso(LocalDateTime.now());
            repository.save(usuario);

            resetLoginAttempts(username);

            auditoriaService.registrarLogin(username, true);

            String token = tokenProvider.generateToken(usuario.getUsername(), usuario.getPerfil().name());
            log.info("Login bem-sucedido: {} - IP: {}", username, ip);

            return new LoginResponse(token, usuario.getUsername(), usuario.getNome(), usuario.getPerfil());

        } catch (RuntimeException e) {
            log.error("Erro no login: {} - {}", username, e.getMessage());
            if (!e.getMessage().contains("Credenciais")) {
                auditoriaService.registrarLogin(username, false);
            }
            throw e;
        }
    }

    private void handleFailedLogin(String username, String ip) {
        int attempts = incrementAttempt(username);
        auditoriaService.registrarLogin(username, false);
        log.warn("Falha de login: {} - Tentativa {}/{} - IP: {}", username, attempts, MAX_ATTEMPTS, ip);

        if (attempts >= MAX_ATTEMPTS) {
            repository.findByUsername(username).ifPresent(usuario -> {
                usuario.incrementarTentativas();
                repository.save(usuario);
            });
            log.warn("Usuário bloqueado após {} tentativas: {}", MAX_ATTEMPTS, username);
        }
    }

    private synchronized int incrementAttempt(String username) {
        AttemptTracker tracker = loginAttempts.computeIfAbsent(username, k -> new AttemptTracker());
        tracker.attempts++;
        tracker.lastAttempt = LocalDateTime.now();
        return tracker.attempts;
    }

    private void resetLoginAttempts(String username) {
        loginAttempts.remove(username);
    }

    private boolean isBlocked(String username) {
        AttemptTracker tracker = loginAttempts.get(username);
        if (tracker == null) return false;

        if (tracker.attempts >= MAX_ATTEMPTS &&
            tracker.lastAttempt.plusMinutes(LOCKOUT_MINUTES).isAfter(LocalDateTime.now())) {
            return true;
        }

        if (tracker.attempts >= MAX_ATTEMPTS) {
            loginAttempts.remove(username);
        }
        return false;
    }

    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Transactional
    public void criarUsuarioAdmin() {
        if (!repository.existsByUsername("admin")) {
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("ADMIN_PASSWORD não configurado — usando senha padrão de desenvolvimento!");
                adminPassword = "Admin@Dev2026";
            }
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode(adminPassword));
            admin.setNome("Administrador do Sistema");
            admin.setPerfil(Usuario.Perfil.ADMINISTRADOR);
            admin.setAtivo(true);
            repository.save(admin);
            log.info("Usuário admin criado com sucesso");
        } else {
            log.debug("Usuário admin já existe, pulando criação");
        }
    }

    private static class AttemptTracker {
        int attempts = 0;
        LocalDateTime lastAttempt;
    }
}