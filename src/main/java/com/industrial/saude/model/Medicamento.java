package com.industrial.saude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicamentos", indexes = {
    @Index(name = "idx_medicamento_nome", columnList = "nome"),
    @Index(name = "idx_medicamento_categoria", columnList = "categoria"),
    @Index(name = "idx_medicamento_ativo", columnList = "ativo")
})
public class Medicamento extends BaseEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "principio_ativo")
    private String principioAtivo;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque = 0;

    @Column(name = "quantidade_minima")
    private Integer quantidadeMinima;

    @Column(name = "unidade")
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private CategoriaMedicamento categoria;

    @Column(name = "data_validade")
    private LocalDateTime dataValidade;

    @Column(name = "lote")
    private String lote;

    public enum CategoriaMedicamento {
        ANALGESICO, ANTIINFLAMATORIO, ANTIBIOTICO, ANTIALERGICO, CURATIVO, SOLUCAO, MATERIAL_DESCARTAVEL
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public void setPrincipioAtivo(String principioAtivo) {
        this.principioAtivo = principioAtivo;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Integer getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(Integer quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public CategoriaMedicamento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMedicamento categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDateTime dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public boolean isEstoqueBaixo() {
        return quantidadeMinima != null && quantidadeEstoque < quantidadeMinima;
    }

    public boolean isVencido() {
        return dataValidade != null && dataValidade.isBefore(LocalDateTime.now());
    }
}