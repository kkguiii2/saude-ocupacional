package com.industrial.saude.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardDTO {
    private long totalColaboradores;
    private long colaboradoresAtivos;
    private long atendimentosHoje;
    private long emergenciasHoje;
    private long acidentesMes;
    private long afastamentosAtivos;
    private long estoqueBaixo;
    private Map<String, Long> atendimentosPorSetor;
    private Map<String, Long> acidentesPorTipo;
    private Map<String, Long> atendimentosPorTipo;
}