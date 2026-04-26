package com.industrial.saude.dto;

import com.industrial.saude.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioRequest {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    /** Obrigatório apenas na criação; opcional na atualização */
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "Nome completo é obrigatório")
    private String nome;

    @NotNull(message = "Perfil é obrigatório")
    private Usuario.Perfil perfil;

    private String matricula;

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Usuario.Perfil getPerfil() { return perfil; }
    public void setPerfil(Usuario.Perfil perfil) { this.perfil = perfil; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
}
