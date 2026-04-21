package com.industrial.saude.service;

import com.industrial.saude.dto.AlertaDTO;
import com.industrial.saude.model.Agendamento;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AgendamentoRepository agendamentoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final AcidenteTrabalhoRepository acidenteRepository;

    public AlertaDTO getAlertas() {
        AlertaDTO alertas = new AlertaDTO();

        // 1. Exames próximos do vencimento (próximos 30 dias)
        List<Agendamento> examesProximos = agendamentoRepository.findAll().stream()
                .filter(a -> {
                    LocalDate data = a.getDataHora().toLocalDate();
                    return data.isAfter(LocalDate.now()) && data.isBefore(LocalDate.now().plusDays(30));
                })
                .collect(Collectors.toList());

        List<AlertaDTO.ExameVencidoAlert> exameAlerts = examesProximos.stream()
                .map(a -> {
                    AlertaDTO.ExameVencidoAlert alert = new AlertaDTO.ExameVencidoAlert();
                    alert.setColaboradorId(a.getColaborador().getId());
                    alert.setNomeColaborador(a.getColaborador().getNomeCompleto());
                    alert.setMatricula(a.getColaborador().getMatricula());
                    alert.setTipoExame(a.getTipo().name());
                    alert.setDataVencimento(a.getDataHora().toLocalDate());
                    long dias = ChronoUnit.DAYS.between(LocalDate.now(), a.getDataHora().toLocalDate());
                    alert.setDiasRestantes(dias);
                    alert.setUrgencia(dias < 15 ? "URGENTE" : "PRIORIDADE");
                    return alert;
                })
                .collect(Collectors.toList());
        alertas.setExamesVencidos(exameAlerts);

        // 2. Afastamentos ativos há mais de 15 dias
        List<Colaborador> afastados = colaboradorRepository.findByStatusFuncionarioAndAtivoTrue(Colaborador.StatusFuncionario.AFASTADO).stream()
                .collect(Collectors.toList());

        List<AlertaDTO.AfastamentoAlert> afastamentoAlerts = afastados.stream()
                .map(c -> {
                    AlertaDTO.AfastamentoAlert alert = new AlertaDTO.AfastamentoAlert();
                    alert.setColaboradorId(c.getId());
                    alert.setNomeColaborador(c.getNomeCompleto());
                    alert.setMatricula(c.getMatricula());
                    alert.setInicioAfastamento(c.getDataAdmissao());
                    alert.setDiasAfastado(ChronoUnit.DAYS.between(c.getDataAdmissao(), LocalDate.now()));
                    alert.setMotivo("Afastamento judicial");
                    return alert;
                })
                .collect(Collectors.toList());
        alertas.setAfastamentos(afastamentoAlerts);

        // 3. Colaboradores de risco alto
        List<Colaborador> riscoAlto = colaboradorRepository.findByTipoRiscoAndAtivoTrue(Colaborador.TipoRisco.ALTO);
        List<AlertaDTO.ColaboradorRiscoAlto> riscoAlerts = riscoAlto.stream()
                .map(c -> {
                    AlertaDTO.ColaboradorRiscoAlto alert = new AlertaDTO.ColaboradorRiscoAlto();
                    alert.setColaboradorId(c.getId());
                    alert.setNomeColaborador(c.getNomeCompleto());
                    alert.setSetor(c.getSetor().name());
                    alert.setCargo(c.getCargo());
                    return alert;
                })
                .collect(Collectors.toList());
        alertas.setColaboradoresRiscoAlto(riscoAlerts);

        // Total de pendências
        alertas.setTotalPendencias((long) exameAlerts.size() + afastamentoAlerts.size() + riscoAlerts.size());

        return alertas;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void verificarAlertasDiarios() {
        log.info("Verificando alertas automáticos...");
        AlertaDTO alertas = getAlertas();
        if (alertas.getTotalPendencias() > 0) {
            log.warn("Alertas encontrados: {} pendências", alertas.getTotalPendencias());
        }
    }
}