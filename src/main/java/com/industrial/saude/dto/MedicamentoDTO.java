package com.industrial.saude.dto;

import com.industrial.saude.model.Medicamento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicamentoDTO {
    private Long id;
    
    @NotNull(message = "Nome é obrigatório")
    private String nome;
    
    private String principioAtivo;
    
    @NotNull(message = "Quantidade é obrigatória")
    private Integer quantidadeEstoque;
    
    private Integer quantidadeMinima;
    private String unidade;
    private Medicamento.CategoriaMedicamento categoria;
    private String dataValidade;
}