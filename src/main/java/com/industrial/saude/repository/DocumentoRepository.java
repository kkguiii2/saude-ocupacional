package com.industrial.saude.repository;

import com.industrial.saude.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByColaboradorId(Long colaboradorId);
    List<Documento> findByTipo(Documento.TipoDocumento tipo);
    Optional<Documento> findByNumeroDocumento(String numero);
}