package com.industrial.saude.service;

import com.industrial.saude.dto.ColaboradorDTO;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.model.ProntuarioOcupacional;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.ColaboradorRepository;
import com.industrial.saude.repository.ProntuarioOcupacionalRepository;
import com.industrial.saude.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColaboradorService {

    private final ColaboradorRepository repository;
    private final ProntuarioOcupacionalRepository prontuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<ColaboradorDTO> findAll(Pageable pageable) {
        return repository.findByAtivoTrue(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ColaboradorDTO> findAtivos() {
        return repository.findByAtivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ColaboradorDTO findById(Long id) {
        return repository.findById(id)
                .filter(c -> c.isAtivo())
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Colaborador não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public ColaboradorDTO findByMatricula(String matricula) {
        return repository.findByMatricula(matricula)
                .filter(c -> c.isAtivo())
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Colaborador não encontrado com matrícula: " + matricula));
    }

    @Transactional(readOnly = true)
    public List<ColaboradorDTO> findBySetor(Colaborador.Setor setor) {
        return repository.findBySetorAndAtivoTrue(setor).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ColaboradorDTO save(ColaboradorDTO dto, String username) {
        if (repository.existsByMatricula(dto.getMatricula())) {
            throw new IllegalArgumentException("Matrícula já existe: " + dto.getMatricula());
        }

        Colaborador entity = toEntity(dto);
        entity = repository.save(entity);

        ProntuarioOcupacional prontuario = new ProntuarioOcupacional();
        prontuario.setColaborador(entity);
        prontuarioRepository.save(prontuario);

        auditoriaService.registrar(username, AuditoriaService.ACAO_CREATE, "COLABORADOR",
                "Novo colaborador criado: " + entity.getNomeCompleto());

        log.info("Colaborador criado: {} - {} por {}", entity.getId(), entity.getNomeCompleto(), username);
        return toDTO(entity);
    }

    @Transactional
    public ColaboradorDTO update(Long id, ColaboradorDTO dto, String username) {
        Colaborador entity = repository.findById(id)
                .filter(c -> c.isAtivo())
                .orElseThrow(() -> new EntityNotFoundException("Colaborador não encontrado com ID: " + id));

        if (repository.existsByMatriculaAndIdNot(dto.getMatricula(), id)) {
            throw new IllegalArgumentException("Matrícula já existe: " + dto.getMatricula());
        }

        entity.setNomeCompleto(dto.getNomeCompleto());
        entity.setSetor(dto.getSetor());
        entity.setCargo(dto.getCargo());
        entity.setTipoRisco(dto.getTipoRisco());
        entity.setEpisObrigatorios(dto.getEpisObrigatorios());
        entity.setContatoEmergencia(dto.getContatoEmergencia());
        entity.setNomeContatoEmergencia(dto.getNomeContatoEmergencia());
        entity.setTelefoneContato(dto.getTelefoneContato());
        entity.setDataAdmissao(dto.getDataAdmissao());
        entity.setStatusFuncionario(dto.getStatusFuncionario());

        auditoriaService.registrar(username, AuditoriaService.ACAO_UPDATE, "COLABORADOR",
                "Colaborador atualizado: " + entity.getNomeCompleto());

        log.info("Colaborador atualizado: {} por {}", id, username);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, String username) {
        Colaborador entity = repository.findById(id)
                .filter(c -> c.isAtivo())
                .orElseThrow(() -> new EntityNotFoundException("Colaborador não encontrado com ID: " + id));

        entity.setAtivo(false);
        entity.setDataAtualizacao(LocalDateTime.now());
        repository.save(entity);

        auditoriaService.registrar(username, AuditoriaService.ACAO_DELETE, "COLABORADOR",
                "Colaborador desativado: " + entity.getNomeCompleto());

        log.info("Colaborador desativado: {} por {}", id, username);
    }

    @Transactional(readOnly = true)
    public long countBySetor(Colaborador.Setor setor) {
        return repository.countBySetor(setor);
    }

    @Transactional(readOnly = true)
    public long countAtivos() {
        return repository.countAtivos();
    }

    private ColaboradorDTO toDTO(Colaborador entity) {
        ColaboradorDTO dto = new ColaboradorDTO();
        dto.setId(entity.getId());
        dto.setNomeCompleto(entity.getNomeCompleto());
        dto.setMatricula(entity.getMatricula());
        dto.setSetor(entity.getSetor());
        dto.setCargo(entity.getCargo());
        dto.setTipoRisco(entity.getTipoRisco());
        dto.setEpisObrigatorios(entity.getEpisObrigatorios());
        dto.setContatoEmergencia(entity.getContatoEmergencia());
        dto.setNomeContatoEmergencia(entity.getNomeContatoEmergencia());
        dto.setTelefoneContato(entity.getTelefoneContato());
        dto.setDataAdmissao(entity.getDataAdmissao());
        dto.setStatusFuncionario(entity.getStatusFuncionario());
        dto.setAtivo(entity.isAtivo());
        dto.setDataCadastro(entity.getDataCadastro());
        return dto;
    }

    private Colaborador toEntity(ColaboradorDTO dto) {
        Colaborador entity = new Colaborador();
        if (dto.getId() != null) entity.setId(dto.getId());
        entity.setNomeCompleto(dto.getNomeCompleto());
        entity.setMatricula(dto.getMatricula());
        entity.setSetor(dto.getSetor());
        entity.setCargo(dto.getCargo());
        entity.setTipoRisco(dto.getTipoRisco());
        entity.setEpisObrigatorios(dto.getEpisObrigatorios());
        entity.setContatoEmergencia(dto.getContatoEmergencia());
        entity.setNomeContatoEmergencia(dto.getNomeContatoEmergencia());
        entity.setTelefoneContato(dto.getTelefoneContato());
        entity.setDataAdmissao(dto.getDataAdmissao());
        entity.setStatusFuncionario(dto.getStatusFuncionario() != null ? dto.getStatusFuncionario() : Colaborador.StatusFuncionario.ATIVO);
        return entity;
    }
}