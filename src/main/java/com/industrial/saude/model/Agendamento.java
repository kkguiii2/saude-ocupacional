package com.industrial.saude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos", indexes = {
    @Index(name = "idx_agendamento_colaborador", columnList = "colaborador_id"),
    @Index(name = "idx_agendamento_data", columnList = "data_hora"),
    @Index(name = "idx_agendamento_status", columnList = "status"),
    @Index(name = "idx_agendamento_tipo", columnList = "tipo")
})
public class Agendamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoExame tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(name = "observacoes")
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendado_por")
    private Usuario agendadoPor;

    @PrePersist
    public void prePersist() {
        if (getDataCadastro() == null) {
            setDataCadastro(LocalDateTime.now());
        }
        if (status == null) {
            status = StatusAgendamento.AGENDADO;
        }
    }

    public enum TipoExame {
        ADMISSIONAL, PERIODICO, DEMISIONAL, RETORNO_AFASTAMENTO, MUDANCA_FUNCAO, EXAME_CLINICO, AUDIOMETRIA, ACUIDADE_VISUAL, ESPIROMETRIA, ECG
    }

    public enum StatusAgendamento {
        AGENDADO, REALIZADO, CANCELADO, FALTOU
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public TipoExame getTipo() {
        return tipo;
    }

    public void setTipo(TipoExame tipo) {
        this.tipo = tipo;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Usuario getAgendadoPor() {
        return agendadoPor;
    }

    public void setAgendadoPor(Usuario agendadoPor) {
        this.agendadoPor = agendadoPor;
    }
}