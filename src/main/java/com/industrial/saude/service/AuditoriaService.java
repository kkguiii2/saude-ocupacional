package com.industrial.saude.service;

import com.industrial.saude.model.AuditoriaLog;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.AuditoriaLogRepository;
import com.industrial.saude.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaLogRepository repository;
    private final UsuarioRepository usuarioRepository;

    public static final String ACAO_CREATE = "CREATE";
    public static final String ACAO_UPDATE = "UPDATE";
    public static final String ACAO_DELETE = "DELETE";
    public static final String ACAO_LOGIN = "LOGIN";
    public static final String ACAO_LOGOUT = "LOGOUT";
    public static final String ACAO_VIEW = "VIEW";
    public static final String ACAO_ENTRADA = "ENTRADA";
    public static final String ACAO_SAIDA = "SAIDA";

    @Transactional
    public void registrar(String username, String acao, String modulo, String descricao) {
        try {
            AuditoriaLog auditoria = new AuditoriaLog();
            auditoria.setAcao(acao);
            auditoria.setModulo(modulo);
            auditoria.setDescricao(descricao);
            auditoria.setDataHora(LocalDateTime.now());
            
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                HttpServletRequest request = attrs.getRequest();
                auditoria.setIp(request.getRemoteAddr());
                auditoria.setUserAgent(request.getHeader("User-Agent"));
            }

            Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
            usuario.ifPresent(auditoria::setUsuario);

            repository.save(auditoria);
            log.info("AUDITORIA: {} - {} - {} - {}", username, acao, modulo, descricao);
        } catch (Exception e) {
            log.error("Erro ao registrar auditoria: ", e);
        }
    }

    @Transactional
    public void registrar(String username, String acao, String modulo, String descricao, String detalhes) {
        try {
            AuditoriaLog auditoria = new AuditoriaLog();
            auditoria.setAcao(acao);
            auditoria.setModulo(modulo);
            auditoria.setDescricao(descricao);
            auditoria.setDetalhes(detalhes);
            auditoria.setDataHora(LocalDateTime.now());

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                HttpServletRequest request = attrs.getRequest();
                auditoria.setIp(request.getRemoteAddr());
                auditoria.setUserAgent(request.getHeader("User-Agent"));
            }

            Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
            usuario.ifPresent(auditoria::setUsuario);

            repository.save(auditoria);
            log.info("AUDITORIA: {} - {} - {} - {}", username, acao, modulo, descricao);
        } catch (Exception e) {
            log.error("Erro ao registrar auditoria: ", e);
        }
    }

    public void registrarLogin(String username, boolean sucesso) {
        try {
            AuditoriaLog auditoria = new AuditoriaLog();
            auditoria.setAcao(sucesso ? "LOGIN_SUCESSO" : "LOGIN_FALHA");
            auditoria.setModulo("AUTH");
            auditoria.setDescricao(sucesso ? "Login realizado com sucesso" : "Falha no login");
            auditoria.setDataHora(LocalDateTime.now());

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                HttpServletRequest request = attrs.getRequest();
                auditoria.setIp(request.getRemoteAddr());
                auditoria.setUserAgent(request.getHeader("User-Agent"));
            }

            if (sucesso) {
                Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
                usuario.ifPresent(auditoria::setUsuario);
            }

            repository.save(auditoria);
            log.info("LOGIN: {} - {}", username, sucesso ? "sucesso" : "falha");
        } catch (Exception e) {
            log.error("Erro ao registrar login: ", e);
        }
    }

    public void registrarLogout(String username) {
        try {
            AuditoriaLog auditoria = new AuditoriaLog();
            auditoria.setAcao("LOGOUT");
            auditoria.setModulo("AUTH");
            auditoria.setDescricao("Logout realizado");
            auditoria.setDataHora(LocalDateTime.now());

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                HttpServletRequest request = attrs.getRequest();
                auditoria.setIp(request.getRemoteAddr());
                auditoria.setUserAgent(request.getHeader("User-Agent"));
            }

            Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
            usuario.ifPresent(auditoria::setUsuario);

            repository.save(auditoria);
            log.info("LOGOUT: {}", username);
        } catch (Exception e) {
            log.error("Erro ao registrar logout: ", e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditoriaLog> buscarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AuditoriaLog> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaLog> buscarPorModulo(String modulo) {
        return repository.findByModulo(modulo);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaLog> buscarPorFiltros(String modulo, Long usuarioId, String acao, LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByFiltros(modulo, usuarioId, acao, inicio, fim);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaLog> buscarTodos(Pageable pageable) {
        return repository.findByOrderByDataHoraDesc(pageable);
    }
}