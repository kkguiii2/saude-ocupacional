package com.industrial.saude.service;

import com.industrial.saude.dto.DashboardDTO;
import com.industrial.saude.model.AcidenteTrabalho;
import com.industrial.saude.model.Atendimento;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.repository.AcidenteTrabalhoRepository;
import com.industrial.saude.repository.AtendimentoRepository;
import com.industrial.saude.repository.ColaboradorRepository;
import com.industrial.saude.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ColaboradorRepository colaboradorRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final AcidenteTrabalhoRepository acidenteRepository;
    private final MedicamentoRepository medicamentoRepository;

    public DashboardDTO getDados() {
        DashboardDTO dto = new DashboardDTO();

        dto.setTotalColaboradores(colaboradorRepository.count());
        dto.setColaboradoresAtivos(colaboradorRepository.countAtivos());
        dto.setAtendimentosHoje(countAtendimentosHoje());
        dto.setEmergenciasHoje(countEmergenciasHoje());
        dto.setAcidentesMes(countAcidentesMes());
        dto.setAfastamentosAtivos(countAfastamentos());
        dto.setEstoqueBaixo(medicamentoRepository.countEstoqueBaixo());

        dto.setAtendimentosPorSetor(getAtendimentosPorSetor());
        dto.setAcidentesPorTipo(getAcidentesPorTipo());
        dto.setAtendimentosPorTipo(getAtendimentosPorTipo());

        return dto;
    }

    private long countAtendimentosHoje() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return atendimentoRepository.countByPeriodo(inicio, fim);
    }

    private long countEmergenciasHoje() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return atendimentoRepository.countEmergenciasByPeriodo(inicio, fim);
    }

    private long countAcidentesMes() {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return acidenteRepository.countByPeriodo(inicio, fim);
    }

    private long countAfastamentos() {
        return colaboradorRepository.countByStatus(Colaborador.StatusFuncionario.AFASTADO);
    }

    private Map<String, Long> getAtendimentosPorSetor() {
        List<Object[]> results = atendimentoRepository.countBySetorGrouped();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put(row[0].toString(), (Long) row[1]);
        }
        return map;
    }

    private Map<String, Long> getAcidentesPorTipo() {
        List<Object[]> results = acidenteRepository.countByTipoGrouped();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put(row[0].toString(), (Long) row[1]);
        }
        return map;
    }

    private Map<String, Long> getAtendimentosPorTipo() {
        List<Object[]> results = atendimentoRepository.countByTipoGrouped();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put(row[0].toString(), (Long) row[1]);
        }
        return map;
    }
}