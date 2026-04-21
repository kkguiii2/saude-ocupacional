package com.industrial.saude.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AlertaDTO {
    private List<ExameVencidoAlert> examesVencidos;
    private List<AfastamentoAlert> afastamentos;
    private List<ColaboradorRiscoAlto> colaboradoresRiscoAlto;
    private long totalPendencias;

    @Data
    public static class ExameVencidoAlert {
        private Long colaboradorId;
        private String nomeColaborador;
        private String matricula;
        private String tipoExame;
        private LocalDate dataVencimento;
        private long diasRestantes;
        private String urgencia;
    }

    @Data
    public static class AfastamentoAlert {
        private Long colaboradorId;
        private String nomeColaborador;
        private String matricula;
        private LocalDate inicioAfastamento;
        private long diasAfastado;
        private String motivo;
    }

    @Data
    public static class ColaboradorRiscoAlto {
        private Long colaboradorId;
        private String nomeColaborador;
        private String setor;
        private String cargo;
    }
}