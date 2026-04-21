package com.industrial.saude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prontuarios_ocupacionais", indexes = {
    @Index(name = "idx_prontuario_colaborador", columnList = "colaborador_id", unique = true)
})
public class ProntuarioOcupacional extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false, unique = true)
    private Colaborador colaborador;

    @Column(name = "historico_doencas", columnDefinition = "TEXT")
    private String historicoDoencas;

    @Column(name = "historico_cirurgias", columnDefinition = "TEXT")
    private String historicoCirurgias;

    @Column(name = "alergias", columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "medicacoes_uso", columnDefinition = "TEXT")
    private String medicacoesUso;

    @Column(name = "restricoes_trabalho", columnDefinition = "TEXT")
    private String restricoesTrabalho;

    @Column(name = "riscos_exposicao", columnDefinition = "TEXT")
    private String riscosExposicao;

    @Column(name = "ultimo_exame")
    private LocalDateTime ultimoExame;

    @Column(name = "proximo_exame")
    private LocalDateTime proximoExame;

    @Column(name = "risco_quimico")
    private boolean riscoQuimico;

    @Column(name = "ruido")
    private boolean ruido;

    @Column(name = "calor")
    private boolean calor;

    @Column(name = "machines")
    private boolean machines;

    @Column(name = "cargas")
    private boolean cargas;

    @Column(name = "observacoes_gerais", columnDefinition = "TEXT")
    private String observacoesGerais;

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }

    public String getHistoricoDoencas() {
        return historicoDoencas;
    }

    public void setHistoricoDoencas(String historicoDoencas) {
        this.historicoDoencas = historicoDoencas;
    }

    public String getHistoricoCirurgias() {
        return historicoCirurgias;
    }

    public void setHistoricoCirurgias(String historicoCirurgias) {
        this.historicoCirurgias = historicoCirurgias;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getMedicacoesUso() {
        return medicacoesUso;
    }

    public void setMedicacoesUso(String medicacoesUso) {
        this.medicacoesUso = medicacoesUso;
    }

    public String getRestricoesTrabalho() {
        return restricoesTrabalho;
    }

    public void setRestricoesTrabalho(String restricoesTrabalho) {
        this.restricoesTrabalho = restricoesTrabalho;
    }

    public String getRiscosExposicao() {
        return riscosExposicao;
    }

    public void setRiscosExposicao(String riscosExposicao) {
        this.riscosExposicao = riscosExposicao;
    }

    public LocalDateTime getUltimoExame() {
        return ultimoExame;
    }

    public void setUltimoExame(LocalDateTime ultimoExame) {
        this.ultimoExame = ultimoExame;
    }

    public LocalDateTime getProximoExame() {
        return proximoExame;
    }

    public void setProximoExame(LocalDateTime proximoExame) {
        this.proximoExame = proximoExame;
    }

    public boolean isRiscoQuimico() {
        return riscoQuimico;
    }

    public void setRiscoQuimico(boolean riscoQuimico) {
        this.riscoQuimico = riscoQuimico;
    }

    public boolean isRuido() {
        return ruido;
    }

    public void setRuido(boolean ruido) {
        this.ruido = ruido;
    }

    public boolean isCalor() {
        return calor;
    }

    public void setCalor(boolean calor) {
        this.calor = calor;
    }

    public boolean isMachines() {
        return machines;
    }

    public void setMachines(boolean machines) {
        this.machines = machines;
    }

    public boolean isCargas() {
        return cargas;
    }

    public void setCargas(boolean cargas) {
        this.cargas = cargas;
    }

    public String getObservacoesGerais() {
        return observacoesGerais;
    }

    public void setObservacoesGerais(String observacoesGerais) {
        this.observacoesGerais = observacoesGerais;
    }
}