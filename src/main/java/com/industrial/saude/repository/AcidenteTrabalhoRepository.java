package com.industrial.saude.repository;

import com.industrial.saude.model.AcidenteTrabalho;
import com.industrial.saude.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AcidenteTrabalhoRepository extends JpaRepository<AcidenteTrabalho, Long> {

    List<AcidenteTrabalho> findByColaboradorId(Long colaboradorId);

    List<AcidenteTrabalho> findByTipoAndAtivoTrue(AcidenteTrabalho.TipoAcidente tipo);

    List<AcidenteTrabalho> findByCatEmitidaAndAtivoTrue(boolean catEmitida);

    @Query("SELECT a FROM AcidenteTrabalho a WHERE a.dataHora BETWEEN :inicio AND :fim AND a.ativo = true")
    List<AcidenteTrabalho> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT a FROM AcidenteTrabalho a WHERE a.colaborador.setor = :setor AND a.ativo = true")
    List<AcidenteTrabalho> findBySetor(@Param("setor") Colaborador.Setor setor);

    @Query("SELECT COUNT(a) FROM AcidenteTrabalho a WHERE a.dataHora BETWEEN :inicio AND :fim AND a.ativo = true")
    long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT a.tipo, COUNT(a) FROM AcidenteTrabalho a WHERE a.ativo = true GROUP BY a.tipo")
    List<Object[]> countByTipoGrouped();

    @Query("SELECT COUNT(a) FROM AcidenteTrabalho a WHERE a.catEmitida = true AND a.ativo = true")
    long countComCat();
}