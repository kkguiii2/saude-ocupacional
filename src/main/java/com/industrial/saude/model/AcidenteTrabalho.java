package com.industrial.saude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "acidentes_trabalho", indexes = {
    @Index(name = "idx_acidente_colaborador", columnList = "colaborador_id"),
    @Index(name = "idx_acidente_data", columnList = "data_hora"),
    @Index(name = "idx_acidente_tipo", columnList = "tipo"),
    @Index(name = "idx_acidente_cat", columnList = "cat_emitida")
})
public class AcidenteTrabalho extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "local_fabrica", nullable = false)
    private String localFabrica;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoAcidente tipo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "causa", columnDefinition = "TEXT")
    private String causa;

    @Column(name = "medidas_tomadas", columnDefinition = "TEXT")
    private String medidasTomadas;

    @Column(name = "testemunhas")
    private String testemunhas;

    @Column(name = "cat_emitida")
    private boolean catEmitida;

    @Column(name = "numero_cat")
    private String numeroCat;

    @Column(name = "data_cat")
    private LocalDateTime dataCat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;

    public enum TipoAcidente {
        CORTE, QUEIMA, QUEDA, ATROPELAMENTO, INTOXICACAO, ESMAGAMENTO, CHOQUE_ELETRICO, QUEDA_OBJETO, OUTROS
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

    public String getLocalFabrica() {
        return localFabrica;
    }

    public void setLocalFabrica(String localFabrica) {
        this.localFabrica = localFabrica;
    }

    public TipoAcidente getTipo() {
        return tipo;
    }

    public void setTipo(TipoAcidente tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCausa() {
        return causa;
    }

    public void setCausa(String causa) {
        this.causa = causa;
    }

    public String getMedidasTomadas() {
        return medidasTomadas;
    }

    public void setMedidasTomadas(String medidasTomadas) {
        this.medidasTomadas = medidasTomadas;
    }

    public String getTestemunhas() {
        return testemunhas;
    }

    public void setTestemunhas(String testemunhas) {
        this.testemunhas = testemunhas;
    }

    public boolean isCatEmitida() {
        return catEmitida;
    }

    public void setCatEmitida(boolean catEmitida) {
        this.catEmitida = catEmitida;
    }

    public String getNumeroCat() {
        return numeroCat;
    }

    public void setNumeroCat(String numeroCat) {
        this.numeroCat = numeroCat;
    }

    public LocalDateTime getDataCat() {
        return dataCat;
    }

    public void setDataCat(LocalDateTime dataCat) {
        this.dataCat = dataCat;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }
}