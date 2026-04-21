package com.industrial.saude.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HistoricoColaboradorDTO {
    private DadosColaboradorDTO dadosColaborador;
    private List<AtendimentoDTO> atendimentos;
    private List<AcidenteTrabalhoDTO> acidentes;
    private List<AgendamentoDTO> agendamentos;
    private AfastamentoDTO afastamentoAtual;
    private List<ExameVencidoDTO> examesVencidos;
    private EstatisticasDTO estatisticas;

    @Data
    public static class DadosColaboradorDTO {
        private Long id;
        private String nomeCompleto;
        private String matricula;
        private String setor;
        private String cargo;
        private String tipoRisco;
        private String statusFuncionario;
        private String episObrigatorios;
        private String contatoEmergencia;
        private String telefoneContato;
        private LocalDate dataAdmissao;
    }

    @Data
    public static class AfastamentoDTO {
        private Long id;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivo;
        private String cids;
        private boolean ativo;
    }

    @Data
    public static class ExameVencidoDTO {
        private String tipoExame;
        private LocalDate dataValidade;
        private long diasRestantes;
        private boolean urgente;
    }

    @Data
    public static class EstatisticasDTO {
        private long totalAtendimentos;
        private long totalAcidentes;
        private long totalExames;
        private long diasAfastado;
    }
}