package com.industrial.saude.dto;

import com.industrial.saude.model.Atendimento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtendimentoDTO {
    private Long id;
    
    private String dataHora;
    private Long colaboradorId;
    private String colaboradorMatricula;
    private String colaboradorNome;
    
    @NotNull(message = "Tipo de atendimento é obrigatório")
    private Atendimento.TipoAtendimento tipo;
    
    private String sintomas;
    
    @NotNull(message = "Gravidade é obrigatória")
    private Atendimento.Gravidade gravidade;
    
    private String conduta;
    private Atendimento.Encaminhamento encaminhamento;
    private boolean emergencia;
    private java.util.List<AtendimentoMedicamentoDTO> medicamentos = new java.util.ArrayList<>();
}