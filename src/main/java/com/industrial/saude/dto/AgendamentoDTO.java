package com.industrial.saude.dto;

import com.industrial.saude.model.Agendamento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgendamentoDTO {
    private Long id;
    
    private String colaboradorMatricula;
    private Long colaboradorId;
    private String colaboradorNome;
    
    @NotNull(message = "Data/hora é obrigatória")
    private String dataHora;
    
    @NotNull(message = "Tipo de exame é obrigatório")
    private Agendamento.TipoExame tipo;
    
    private Agendamento.StatusAgendamento status;
    private String observacoes;
}