package com.industrial.saude.dto;

import com.industrial.saude.model.Usuario;
import java.time.LocalDateTime;

public class UsuarioResponse {

    private Long id;
    private String username;
    private String nome;
    private String matricula;
    private Usuario.Perfil perfil;
    private boolean ativo;
    private LocalDateTime ultimoAcesso;
    private LocalDateTime dataCadastro;

    public UsuarioResponse() {}

    public UsuarioResponse(Usuario u) {
        this.id           = u.getId();
        this.username     = u.getUsername();
        this.nome         = u.getNome();
        this.matricula    = u.getMatricula();
        this.perfil       = u.getPerfil();
        this.ativo        = u.isAtivo();
        this.ultimoAcesso = u.getUltimoAcesso();
        this.dataCadastro = u.getDataCadastro();
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getUsername()             { return username; }
    public String getNome()                 { return nome; }
    public String getMatricula()            { return matricula; }
    public Usuario.Perfil getPerfil()       { return perfil; }
    public boolean isAtivo()                { return ativo; }
    public LocalDateTime getUltimoAcesso()  { return ultimoAcesso; }
    public LocalDateTime getDataCadastro()  { return dataCadastro; }
}
