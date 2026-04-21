package com.industrial.saude.dto;

import lombok.Data;

@Data
public class ProntuarioOcupacionalDTO {
    private Long id;
    private Long colaboradorId;
    private String colaboradorNome;
    private String matricula;
    private String historicoDoencas;
    private String historicoCirurgias;
    private String alergias;
    private String medicacoesUso;
    private String restricoesTrabalho;
    private String riscosExposicao;
    private String ultimoExame;
    private String proximoExame;
    private boolean riscoQuimico;
    private boolean ruido;
    private boolean calor;
    private boolean machines;
    private boolean cargas;
    private String observacoesGerais;
}