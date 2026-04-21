package com.industrial.saude.dto;

import com.industrial.saude.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String nome;
    private Usuario.Perfil perfil;
}