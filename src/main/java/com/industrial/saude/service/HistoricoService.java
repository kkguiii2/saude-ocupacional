package com.industrial.saude.service;

import com.industrial.saude.dto.*;
import com.industrial.saude.model.*;
import com.industrial.saude.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final ColaboradorRepository colaboradorRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final AcidenteTrabalhoRepository acidenteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ProntuarioOcupacionalRepository prontuarioRepository;

    public HistoricoColaboradorDTO getHistoricoCompleto(Long colaboradorId) {
        Colaborador collaborator = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        HistoricoColaboradorDTO historico = new HistoricoColaboradorDTO();

        historico.setDadosColaborador(mapToDados(collaborator));

        List<AtendimentoDTO> atendimentos = atendimentoRepository.findByColaboradorId(colaboradorId)
                .stream().map(this::toAtendimentoDTO).collect(Collectors.toList());
        historico.setAtendimentos(atendimentos);

        List<AcidenteTrabalhoDTO> acidentes = acidenteRepository.findByColaboradorId(colaboradorId)
                .stream().map(this::toAcidenteDTO).collect(Collectors.toList());
        historico.setAcidentes(acidentes);

        List<AgendamentoDTO> agendamentos = agendamentoRepository.findByColaboradorId(colaboradorId)
                .stream().map(this::toAgendamentoDTO).collect(Collectors.toList());
        historico.setAgendamentos(agendamentos);

        historico.setExamesVencidos(verificarExamesVencidosEntity(agendamentoRepository.findByColaboradorId(colaboradorId)));

        historico.setEstatisticas(calcularEstatisticas(atendimentos, acidentes, collaborator));

        return historico;
    }

    public List<HistoricoColaboradorDTO.ExameVencidoDTO> getExamesVencidosGlobal() {
        LocalDate limite = LocalDate.now().plusDays(30);
        List<Agendamento> todosAgendamentos = agendamentoRepository.findAll();

        return todosAgendamentos.stream()
                .filter(a -> a.getDataHora().toLocalDate().isBefore(limite))
                .filter(a -> a.getDataHora().toLocalDate().isAfter(LocalDate.now()))
                .map(a -> toExameVencidoDTO(a))
                .collect(Collectors.toList());
    }

    private List<HistoricoColaboradorDTO.ExameVencidoDTO> verificarExamesVencidosEntity(List<Agendamento> agendamentos) {
        LocalDate limite = LocalDate.now().plusDays(30);
        return agendamentos.stream()
                .filter(a -> a.getDataHora().toLocalDate().isBefore(limite))
                .filter(a -> a.getDataHora().toLocalDate().isAfter(LocalDate.now()))
                .map(this::toExameVencidoDTO)
                .collect(Collectors.toList());
    }

    private HistoricoColaboradorDTO.ExameVencidoDTO toExameVencidoDTO(Agendamento a) {
        HistoricoColaboradorDTO.ExameVencidoDTO dto = new HistoricoColaboradorDTO.ExameVencidoDTO();
        dto.setTipoExame(a.getTipo().name());
        dto.setDataValidade(a.getDataHora().toLocalDate());
        dto.setDiasRestantes(ChronoUnit.DAYS.between(LocalDate.now(), a.getDataHora().toLocalDate()));
        dto.setUrgente(dto.getDiasRestantes() < 15);
        return dto;
    }

    private HistoricoColaboradorDTO.DadosColaboradorDTO mapToDados(Colaborador c) {
        HistoricoColaboradorDTO.DadosColaboradorDTO dto = new HistoricoColaboradorDTO.DadosColaboradorDTO();
        dto.setId(c.getId());
        dto.setNomeCompleto(c.getNomeCompleto());
        dto.setMatricula(c.getMatricula());
        dto.setSetor(c.getSetor().name());
        dto.setCargo(c.getCargo());
        dto.setTipoRisco(c.getTipoRisco().name());
        dto.setStatusFuncionario(c.getStatusFuncionario().name());
        dto.setEpisObrigatorios(c.getEpisObrigatorios());
        dto.setContatoEmergencia(c.getNomeContatoEmergencia());
        dto.setTelefoneContato(c.getTelefoneContato());
        dto.setDataAdmissao(c.getDataAdmissao());
        return dto;
    }

    private HistoricoColaboradorDTO.EstatisticasDTO calcularEstatisticas(
            List<AtendimentoDTO> atendimentos,
            List<AcidenteTrabalhoDTO> acidentes,
            Colaborador colaborador) {

        HistoricoColaboradorDTO.EstatisticasDTO stats = new HistoricoColaboradorDTO.EstatisticasDTO();
        stats.setTotalAtendimentos(atendimentos.size());
        stats.setTotalAcidentes(acidentes.size());
        stats.setTotalExames(atendimentos.stream()
                .filter(a -> a.getTipo() != null && a.getTipo().name().startsWith("EXAME"))
                .count());

        if (colaborador.getStatusFuncionario() == Colaborador.StatusFuncionario.AFASTADO) {
            long dias = ChronoUnit.DAYS.between(colaborador.getDataAdmissao(), LocalDate.now());
            stats.setDiasAfastado(dias);
        } else {
            stats.setDiasAfastado(0);
        }

        return stats;
    }

    private AtendimentoDTO toAtendimentoDTO(Atendimento a) {
        AtendimentoDTO dto = new AtendimentoDTO();
        dto.setId(a.getId());
        dto.setColaboradorId(a.getColaborador().getId());
        dto.setColaboradorNome(a.getColaborador().getNomeCompleto());
        dto.setColaboradorMatricula(a.getColaborador().getMatricula());
        dto.setTipo(a.getTipo());
        dto.setSintomas(a.getSintomas());
        dto.setGravidade(a.getGravidade());
        dto.setConduta(a.getConduta());
        dto.setEncaminhamento(a.getEncaminhamento());
        dto.setEmergencia(a.isEmergencia());
        return dto;
    }

    private AcidenteTrabalhoDTO toAcidenteDTO(AcidenteTrabalho a) {
        AcidenteTrabalhoDTO dto = new AcidenteTrabalhoDTO();
        dto.setId(a.getId());
        dto.setColaboradorId(a.getColaborador().getId());
        dto.setColaboradorNome(a.getColaborador().getNomeCompleto());
        dto.setTipo(a.getTipo());
        dto.setDescricao(a.getDescricao());
        dto.setLocalFabrica(a.getLocalFabrica());
        dto.setCatEmitida(a.isCatEmitida());
        return dto;
    }

    private AgendamentoDTO toAgendamentoDTO(Agendamento a) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setId(a.getId());
        dto.setColaboradorId(a.getColaborador().getId());
        dto.setColaboradorNome(a.getColaborador().getNomeCompleto());
        dto.setColaboradorMatricula(a.getColaborador().getMatricula());
        dto.setTipo(a.getTipo());
        dto.setDataHora(a.getDataHora().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setStatus(a.getStatus());
        dto.setObservacoes(a.getObservacoes());
        return dto;
    }
}