package com.industrial.saude.service;

import com.industrial.saude.dto.AtendimentoDTO;
import com.industrial.saude.model.Atendimento;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.AtendimentoRepository;
import com.industrial.saude.repository.ColaboradorRepository;
import com.industrial.saude.repository.UsuarioRepository;
import com.industrial.saude.repository.MedicamentoRepository;
import com.industrial.saude.model.AtendimentoMedicamento;
import com.industrial.saude.model.Medicamento;
import com.industrial.saude.dto.AtendimentoMedicamentoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoRepository repository;
    private final ColaboradorRepository colaboradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final EstoqueService estoqueService;
    private final AuditoriaService auditoriaService;
    
    public List<AtendimentoDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AtendimentoDTO> findByColaborador(Long colaboradorId) {
        return repository.findByColaboradorId(colaboradorId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AtendimentoDTO> findHoje() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return repository.findByPeriodo(inicio, fim).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AtendimentoDTO> findEmergencias() {
        return repository.findByEmergenciaAndAtivoTrue(true).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public AtendimentoDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO).orElse(null);
    }
    
    @Transactional
    public AtendimentoDTO save(AtendimentoDTO dto, Long atendenteId) {
        Atendimento entity = new Atendimento();
        
        Colaborador colaborador;
        if (dto.getColaboradorMatricula() != null && !dto.getColaboradorMatricula().isEmpty()) {
            colaborador = colaboradorRepository.findByMatricula(dto.getColaboradorMatricula())
                    .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com a matrícula: " + dto.getColaboradorMatricula()));
        } else {
            throw new RuntimeException("Matrícula do colaborador é obrigatória");
        }
        entity.setColaborador(colaborador);
        
        Usuario atendente = usuarioRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));
        entity.setAtendente(atendente);
        
        if (dto.getDataHora() != null && !dto.getDataHora().isEmpty()) {
            // HTML datetime-local envia "2026-04-21T15:00" (sem segundos).
            // LocalDateTime.parse() exige formato ISO completo; normalizamos aqui.
            String dataHoraStr = dto.getDataHora();
            if (dataHoraStr.length() == 16) { // yyyy-MM-ddTHH:mm
                dataHoraStr = dataHoraStr + ":00";
            }
            entity.setDataHora(LocalDateTime.parse(dataHoraStr));
        } else {
            entity.setDataHora(LocalDateTime.now());
        }
        entity.setTipo(dto.getTipo());
        entity.setSintomas(dto.getSintomas());
        entity.setGravidade(dto.getGravidade());
        entity.setConduta(dto.getConduta());
        entity.setEncaminhamento(dto.getEncaminhamento());
        entity.setEmergencia(dto.isEmergencia());
        
        entity = repository.save(entity);
        String username = atendente.getUsername();

        if (dto.getMedicamentos() != null && !dto.getMedicamentos().isEmpty()) {
            for (AtendimentoMedicamentoDTO medDto : dto.getMedicamentos()) {
                Medicamento med = medicamentoRepository.findById(medDto.getMedicamentoId())
                        .orElseThrow(() -> new RuntimeException("Medicamento não encontrado"));
                AtendimentoMedicamento am = new AtendimentoMedicamento();
                am.setMedicamento(med);
                am.setQuantidade(medDto.getQuantidade());
                entity.addMedicamento(am);
                // Deduct from stock
                estoqueService.saida(med.getId(), medDto.getQuantidade(), atendenteId);
            }
            entity = repository.save(entity);
        }

        auditoriaService.registrar(username, AuditoriaService.ACAO_CREATE, "ATENDAMENTO",
                "Atendimento criado para: " + entity.getColaborador().getNomeCompleto());

        return toDTO(entity);
    }
    
    @Transactional
    public AtendimentoDTO update(Long id, AtendimentoDTO dto) {
        Atendimento entity = repository.findById(id).orElse(null);
        if (entity == null) return null;
        
        entity.setTipo(dto.getTipo());
        entity.setSintomas(dto.getSintomas());
        entity.setGravidade(dto.getGravidade());
        entity.setConduta(dto.getConduta());
        entity.setEncaminhamento(dto.getEncaminhamento());
        entity.setEmergencia(dto.isEmergencia());

        repository.save(entity);

        String username = entity.getAtendente() != null ? entity.getAtendente().getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_UPDATE, "ATENDAMENTO",
                "Atendimento atualizado ID: " + id);

        return toDTO(entity);
    }
    
    public long countHoje() {
        return repository.findByPeriodo(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).size();
    }
    
    public long countEmergencias() {
        return repository.countEmergencias();
    }
    
    private AtendimentoDTO toDTO(Atendimento entity) {
        AtendimentoDTO dto = new AtendimentoDTO();
        dto.setId(entity.getId());
        dto.setDataHora(entity.getDataHora() != null ? entity.getDataHora().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        dto.setColaboradorId(entity.getColaborador().getId());
        dto.setColaboradorNome(entity.getColaborador().getNomeCompleto());
        dto.setColaboradorMatricula(entity.getColaborador().getMatricula());
        dto.setTipo(entity.getTipo());
        dto.setSintomas(entity.getSintomas());
        dto.setGravidade(entity.getGravidade());
        dto.setConduta(entity.getConduta());
        dto.setEncaminhamento(entity.getEncaminhamento());
        dto.setEmergencia(entity.isEmergencia());
        
        if (entity.getMedicamentosDispensados() != null) {
            java.util.List<AtendimentoMedicamentoDTO> meds = new java.util.ArrayList<>();
            for (AtendimentoMedicamento am : entity.getMedicamentosDispensados()) {
                AtendimentoMedicamentoDTO md = new AtendimentoMedicamentoDTO();
                md.setId(am.getId());
                md.setMedicamentoId(am.getMedicamento().getId());
                md.setMedicamentoNome(am.getMedicamento().getNome());
                md.setQuantidade(am.getQuantidade());
                meds.add(md);
            }
            dto.setMedicamentos(meds);
        }
        
        return dto;
    }
}