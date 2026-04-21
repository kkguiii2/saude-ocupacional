package com.industrial.saude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos", indexes = {
    @Index(name = "idx_documento_colaborador", columnList = "colaborador_id"),
    @Index(name = "idx_documento_tipo", columnList = "tipo"),
    @Index(name = "idx_documento_numero", columnList = "numero_documento")
})
public class Documento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoDocumento tipo;

    @Column(name = "conteudo", columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "data_validade")
    private LocalDateTime dataValidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emitido_por")
    private Usuario emitidoPor;

    @Column(name = "assinatura_valida")
    private boolean assinaturaValida;

    @Column(name = "hash_documento")
    private String hashDocumento;

    public enum TipoDocumento {
        ATESTADO, DECLARACAO_COMPARECIMENTO, CAT, ENCAMINHAMENTO_MEDICO, RELATORIO_EXAME, PRONTUARIO, LAUDO_MEDICO
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDocumento tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDateTime getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDateTime dataValidade) {
        this.dataValidade = dataValidade;
    }

    public Usuario getEmitidoPor() {
        return emitidoPor;
    }

    public void setEmitidoPor(Usuario emitidoPor) {
        this.emitidoPor = emitidoPor;
    }

    public boolean isAssinaturaValida() {
        return assinaturaValida;
    }

    public void setAssinaturaValida(boolean assinaturaValida) {
        this.assinaturaValida = assinaturaValida;
    }

    public String getHashDocumento() {
        return hashDocumento;
    }

    public void setHashDocumento(String hashDocumento) {
        this.hashDocumento = hashDocumento;
    }
}