package com.industrial.saude.service;

import com.industrial.saude.dto.AgendamentoDTO;
import com.industrial.saude.model.Agendamento;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.AgendamentoRepository;
import com.industrial.saude.repository.ColaboradorRepository;
import com.industrial.saude.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final ColaboradorRepository colaboradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public List<AgendamentoDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AgendamentoDTO> findPendentes() {
        return repository.findPendentes(LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AgendamentoDTO> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByPeriodo(inicio, fim).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AgendamentoDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO).orElse(null);
    }

    @Transactional
    public AgendamentoDTO save(AgendamentoDTO dto, Long usuarioId) {
        Agendamento entity = new Agendamento();

        Colaborador colaborador;
        if (dto.getColaboradorMatricula() != null && !dto.getColaboradorMatricula().isEmpty()) {
            colaborador = colaboradorRepository.findByMatricula(dto.getColaboradorMatricula())
                    .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com a matrícula: " + dto.getColaboradorMatricula()));
        } else {
            throw new RuntimeException("Matrícula do colaborador é obrigatória");
        }
        entity.setColaborador(colaborador);

        entity.setDataHora(LocalDateTime.parse(dto.getDataHora()));
        entity.setTipo(dto.getTipo());
        entity.setStatus(Agendamento.StatusAgendamento.AGENDADO);
        entity.setObservacoes(dto.getObservacoes());

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        entity.setAgendadoPor(usuario);

        entity = repository.save(entity);

        String username = usuario != null ? usuario.getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_CREATE, "AGENDAMENTO",
                "Agendamento criado para: " + entity.getColaborador().getNomeCompleto());

        return toDTO(entity);
    }

    @Transactional
    public AgendamentoDTO update(Long id, AgendamentoDTO dto) {
        Agendamento entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        entity.setDataHora(LocalDateTime.parse(dto.getDataHora()));
        entity.setTipo(dto.getTipo());
        entity.setStatus(dto.getStatus());
        entity.setObservacoes(dto.getObservacoes());

        repository.save(entity);

        String username = entity.getAgendadoPor() != null ? entity.getAgendadoPor().getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_UPDATE, "AGENDAMENTO",
                "Agendamento atualizado ID: " + id);

        return toDTO(entity);
    }

    @Transactional
    public AgendamentoDTO realizar(Long id) {
        Agendamento entity = repository.findById(id).orElse(null);
        if (entity == null) return null;
        entity.setStatus(Agendamento.StatusAgendamento.REALIZADO);
        repository.save(entity);

        String username = entity.getAgendadoPor() != null ? entity.getAgendadoPor().getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_UPDATE, "AGENDAMENTO",
                "Agendamento realizado ID: " + id);

        return toDTO(entity);
    }

    @Transactional
    public AgendamentoDTO cancelar(Long id) {
        Agendamento entity = repository.findById(id).orElse(null);
        if (entity == null) return null;
        entity.setStatus(Agendamento.StatusAgendamento.CANCELADO);
        repository.save(entity);

        String username = entity.getAgendadoPor() != null ? entity.getAgendadoPor().getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_UPDATE, "AGENDAMENTO",
                "Agendamento cancelado ID: " + id);

        return toDTO(entity);
    }

    private AgendamentoDTO toDTO(Agendamento entity) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setId(entity.getId());
        dto.setColaboradorId(entity.getColaborador().getId());
        dto.setColaboradorNome(entity.getColaborador().getNomeCompleto());
        dto.setColaboradorMatricula(entity.getColaborador().getMatricula());
        dto.setDataHora(entity.getDataHora().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setTipo(entity.getTipo());
        dto.setStatus(entity.getStatus());
        dto.setObservacoes(entity.getObservacoes());
        return dto;
    }
}