package com.industrial.saude.dto;

import com.industrial.saude.model.AcidenteTrabalho;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AcidenteTrabalhoDTO {
    private Long id;
    
    private String colaboradorMatricula;
    private Long colaboradorId;
    private String colaboradorNome;
    private String setor;
    
    @NotNull(message = "Data/hora é obrigatória")
    private String dataHora;
    
    private String localFabrica;
    
    @NotNull(message = "Tipo de acidente é obrigatório")
    private AcidenteTrabalho.TipoAcidente tipo;
    
    private String descricao;
    private String causa;
    private String medidasTomadas;
    private String testemunhas;
    private boolean catEmitida;
    private String numeroCat;
}