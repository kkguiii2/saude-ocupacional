package com.industrial.saude.dto;

import com.industrial.saude.model.Colaborador;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ColaboradorDTO {
    private Long id;

    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @NotBlank(message = "Matrícula é obrigatória")
    private String matricula;

    @NotNull(message = "Setor é obrigatório")
    private Colaborador.Setor setor;

    @NotBlank(message = "Cargo é obrigatório")
    private String cargo;

    @NotNull(message = "Tipo de risco é obrigatório")
    private Colaborador.TipoRisco tipoRisco;

    private String episObrigatorios;
    private String contatoEmergencia;
    private String nomeContatoEmergencia;
    private String telefoneContato;
    private LocalDate dataAdmissao;
    private LocalDate dataNascimento;
    private String telefone;
    private String pisPasep;
    private Colaborador.StatusFuncionario statusFuncionario;
    private boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}