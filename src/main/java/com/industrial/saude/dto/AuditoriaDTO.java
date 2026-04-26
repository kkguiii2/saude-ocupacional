package com.industrial.saude.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditoriaDTO {
    private Long id;
    private String username;
    private String acao;
    private String modulo;
    private String descricao;
    private String detalhes;
    private String ip;
    private String userAgent;
    private LocalDateTime dataHora;
}