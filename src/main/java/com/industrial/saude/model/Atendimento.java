package com.industrial.saude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "atendimentos", indexes = {
    @Index(name = "idx_atendimento_colaborador", columnList = "colaborador_id"),
    @Index(name = "idx_atendimento_atendente", columnList = "atendente_id"),
    @Index(name = "idx_atendimento_data", columnList = "data_hora"),
    @Index(name = "idx_atendimento_tipo", columnList = "tipo"),
    @Index(name = "idx_atendimento_emergencia", columnList = "emergencia")
})
public class Atendimento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id", nullable = false)
    private Usuario atendente;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoAtendimento tipo;

    @Column(name = "sintomas", columnDefinition = "TEXT")
    private String sintomas;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravidade", nullable = false)
    private Gravidade gravidade;

    @Column(name = "conduta", columnDefinition = "TEXT")
    private String conduta;

    @Enumerated(EnumType.STRING)
    @Column(name = "encaminhamento")
    private Encaminhamento encaminhamento;

    @Column(name = "emergencia", nullable = false)
    private boolean emergencia;

    @OneToMany(mappedBy = "atendimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<AtendimentoMedicamento> medicamentosDispensados = new java.util.ArrayList<>();

    public enum TipoAtendimento {
        CONSULTA_ROTINA, EMERGENCIA, ACIDENTE_TRABALHO, RETORNO_TRABALHO, EXAME_PERIODICO, AVALIACAO_CLINICA
    }

    public enum Gravidade {
        LEVE, MODERADA, GRAVE, CRITICA
    }

    public enum Encaminhamento {
        RETORNO_TRABALHO, AFASTAMENTO, ENCAMINHAMENTO_HOSPITAL, REDE_DESENVOLVEDORA, ACOMPANHAMENTO_AMBULATORIO
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }

    public Usuario getAtendente() {
        return atendente;
    }

    public void setAtendente(Usuario atendente) {
        this.atendente = atendente;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public TipoAtendimento getTipo() {
        return tipo;
    }

    public void setTipo(TipoAtendimento tipo) {
        this.tipo = tipo;
    }

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public Gravidade getGravidade() {
        return gravidade;
    }

    public void setGravidade(Gravidade gravidade) {
        this.gravidade = gravidade;
    }

    public String getConduta() {
        return conduta;
    }

    public void setConduta(String conduta) {
        this.conduta = conduta;
    }

    public Encaminhamento getEncaminhamento() {
        return encaminhamento;
    }

    public void setEncaminhamento(Encaminhamento encaminhamento) {
        this.encaminhamento = encaminhamento;
    }

    public boolean isEmergencia() {
        return emergencia;
    }

    public void setEmergencia(boolean emergencia) {
        this.emergencia = emergencia;
    }

    public java.util.List<AtendimentoMedicamento> getMedicamentosDispensados() {
        return medicamentosDispensados;
    }

    public void setMedicamentosDispensados(java.util.List<AtendimentoMedicamento> medicamentosDispensados) {
        this.medicamentosDispensados = medicamentosDispensados;
    }

    public void addMedicamento(AtendimentoMedicamento am) {
        medicamentosDispensados.add(am);
        am.setAtendimento(this);
    }
}