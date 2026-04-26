package com.industrial.saude.dto;

import lombok.Data;

@Data
public class AtendimentoMedicamentoDTO {
    private Long id;
    private Long medicamentoId;
    private String medicamentoNome;
    private Integer quantidade;
}
