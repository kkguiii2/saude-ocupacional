package com.industrial.saude.service;

import com.industrial.saude.dto.UsuarioRequest;
import com.industrial.saude.dto.UsuarioResponse;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoriaService;

    /** Lista todos os usuários (sem expor senha) */
    public List<UsuarioResponse> listarTodos() {
        return repository.findAll()
                .stream()
                .map(UsuarioResponse::new)
                .collect(Collectors.toList());
    }

    /** Busca um usuário por ID */
    public UsuarioResponse buscarPorId(Long id) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));
        return new UsuarioResponse(u);
    }

    /** Cria um novo usuário */
    @Transactional
    public UsuarioResponse criar(UsuarioRequest req, String operador) {
        if (repository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username já existe: " + req.getUsername());
        }
        if (!StringUtils.hasText(req.getPassword())) {
            throw new RuntimeException("Senha é obrigatória na criação do usuário");
        }

        Usuario u = new Usuario();
        u.setUsername(req.getUsername().trim().toLowerCase());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setNome(req.getNome());
        u.setPerfil(req.getPerfil());
        u.setMatricula(req.getMatricula());
        u.setAtivo(true);

        repository.save(u);

        auditoriaService.registrar(
                operador,
                AuditoriaService.ACAO_CREATE,
                "USUARIOS",
                "Usuário criado: " + u.getUsername() + " | Perfil: " + u.getPerfil()
        );

        log.info("Usuário criado: {} por {}", u.getUsername(), operador);
        return new UsuarioResponse(u);
    }

    /** Atualiza dados de um usuário (senha só atualiza se informada) */
    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest req, String operador) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));

        // Impede editar o admin caso não seja o próprio admin editando a si mesmo
        if ("admin".equalsIgnoreCase(u.getUsername()) && !u.getUsername().equalsIgnoreCase(operador)) {
            throw new RuntimeException("O usuário admin só pode ser editado por ele mesmo");
        }

        // Username: só altera se não estiver em uso por outro usuário
        if (!u.getUsername().equalsIgnoreCase(req.getUsername())) {
            if (repository.existsByUsername(req.getUsername())) {
                throw new RuntimeException("Username já existe: " + req.getUsername());
            }
            u.setUsername(req.getUsername().trim().toLowerCase());
        }

        u.setNome(req.getNome());
        u.setPerfil(req.getPerfil());
        u.setMatricula(req.getMatricula());

        if (StringUtils.hasText(req.getPassword())) {
            if (req.getPassword().length() < 6) {
                throw new RuntimeException("Senha deve ter no mínimo 6 caracteres");
            }
            u.setPassword(encoder.encode(req.getPassword()));
        }

        repository.save(u);

        auditoriaService.registrar(
                operador,
                AuditoriaService.ACAO_UPDATE,
                "USUARIOS",
                "Usuário editado: " + u.getUsername() + " | Perfil: " + u.getPerfil()
        );

        log.info("Usuário atualizado: {} por {}", u.getUsername(), operador);
        return new UsuarioResponse(u);
    }

    /** Ativa ou desativa um usuário */
    @Transactional
    public UsuarioResponse alterarStatus(Long id, boolean ativo, String operador) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));

        if ("admin".equalsIgnoreCase(u.getUsername())) {
            throw new RuntimeException("Não é possível desativar o usuário administrador principal");
        }

        u.setAtivo(ativo);
        repository.save(u);

        String acao = ativo ? "REATIVACAO_USUARIO" : "DESATIVACAO_USUARIO";
        auditoriaService.registrar(
                operador,
                AuditoriaService.ACAO_UPDATE,
                "USUARIOS",
                "Usuário " + (ativo ? "reativado" : "desativado") + ": " + u.getUsername()
        );

        log.info("Status do usuário '{}' alterado para {} por {}", u.getUsername(), ativo, operador);
        return new UsuarioResponse(u);
    }

    /** Redefine a senha de um usuário */
    @Transactional
    public void redefinirSenha(Long id, String novaSenha, String operador) {
        if (!StringUtils.hasText(novaSenha) || novaSenha.length() < 6) {
            throw new RuntimeException("Nova senha deve ter no mínimo 6 caracteres");
        }

        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));

        u.setPassword(encoder.encode(novaSenha));
        u.resetarTentativas(); // limpa bloqueio caso estivesse bloqueado
        repository.save(u);

        auditoriaService.registrar(
                operador,
                AuditoriaService.ACAO_UPDATE,
                "USUARIOS",
                "Senha redefinida para usuário: " + u.getUsername()
        );

        log.info("Senha redefinida para '{}' por {}", u.getUsername(), operador);
    }
}
