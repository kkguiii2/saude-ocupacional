package com.industrial.saude.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colaboradores", indexes = {
    @Index(name = "idx_colaborador_matricula", columnList = "matricula", unique = true),
    @Index(name = "idx_colaborador_setor", columnList = "setor"),
    @Index(name = "idx_colaborador_status", columnList = "status_funcionario"),
    @Index(name = "idx_colaborador_ativo", columnList = "ativo"),
    @Index(name = "idx_colaborador_risco", columnList = "tipo_risco")
})
public class Colaborador extends BaseEntity {

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(name = "matricula", nullable = false, unique = true)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(name = "setor", nullable = false)
    private Setor setor;

    @Column(name = "cargo", nullable = false)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_risco", nullable = false)
    private TipoRisco tipoRisco;

    @Column(name = "epis_obrigatorios")
    private String episObrigatorios;

    @Column(name = "contato_emergencia")
    private String contatoEmergencia;

    @Column(name = "nome_contato_emergencia")
    private String nomeContatoEmergencia;

    @Column(name = "telefone_contato")
    private String telefoneContato;

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_funcionario")
    private StatusFuncionario statusFuncionario = StatusFuncionario.ATIVO;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Atendimento> atendimentos = new ArrayList<>();

    @OneToOne(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProntuarioOcupacional prontuario;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL)
    private List<Agendamento> agendamentos = new ArrayList<>();

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL)
    private List<AcidenteTrabalho> acidentes = new ArrayList<>();

    public enum Setor {
        CQ_EXTRUSAO, MATERIAIS, TI_SUPORTE, COMPRAS, FISCAL, RH, MANUTENCAO_INDUSTRIAL, EXTRUSAO
    }

    public enum TipoRisco {
        BAIXO, MEDIO, ALTO
    }

    public enum StatusFuncionario {
        ATIVO, AFASTADO, EM_TRATAMENTO, DEMITIDO
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setSetor(Setor setor) {
        this.setor = setor;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public TipoRisco getTipoRisco() {
        return tipoRisco;
    }

    public void setTipoRisco(TipoRisco tipoRisco) {
        this.tipoRisco = tipoRisco;
    }

    public String getEpisObrigatorios() {
        return episObrigatorios;
    }

    public void setEpisObrigatorios(String episObrigatorios) {
        this.episObrigatorios = episObrigatorios;
    }

    public String getContatoEmergencia() {
        return contatoEmergencia;
    }

    public void setContatoEmergencia(String contatoEmergencia) {
        this.contatoEmergencia = contatoEmergencia;
    }

    public String getNomeContatoEmergencia() {
        return nomeContatoEmergencia;
    }

    public void setNomeContatoEmergencia(String nomeContatoEmergencia) {
        this.nomeContatoEmergencia = nomeContatoEmergencia;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public void setTelefoneContato(String telefoneContato) {
        this.telefoneContato = telefoneContato;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public StatusFuncionario getStatusFuncionario() {
        return statusFuncionario;
    }

    public void setStatusFuncionario(StatusFuncionario statusFuncionario) {
        this.statusFuncionario = statusFuncionario;
    }

    public List<Atendimento> getAtendimentos() {
        return atendimentos;
    }

    public void setAtendimentos(List<Atendimento> atendimentos) {
        this.atendimentos = atendimentos;
    }

    public ProntuarioOcupacional getProntuario() {
        return prontuario;
    }

    public void setProntuario(ProntuarioOcupacional prontuario) {
        this.prontuario = prontuario;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public List<AcidenteTrabalho> getAcidentes() {
        return acidentes;
    }

    public void setAcidentes(List<AcidenteTrabalho> acidentes) {
        this.acidentes = acidentes;
    }

    public void addAtendimento(Atendimento atendimento) {
        atendimentos.add(atendimento);
        atendimento.setColaborador(this);
    }

    public void addAgendamento(Agendamento agendamento) {
        agendamentos.add(agendamento);
        agendamento.setColaborador(this);
    }

    public void addAcidente(AcidenteTrabalho acidente) {
        acidentes.add(acidente);
        acidente.setColaborador(this);
    }
}