package com.industrial.saude.repository;

import com.industrial.saude.model.Atendimento;
import com.industrial.saude.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    List<Atendimento> findByColaboradorId(Long colaboradorId);

    List<Atendimento> findByAtendenteId(Long atendenteId);

    List<Atendimento> findByTipo(Atendimento.TipoAtendimento tipo);

    List<Atendimento> findByEmergenciaAndAtivoTrue(boolean emergencia);

    @Query("SELECT a FROM Atendimento a WHERE a.dataHora BETWEEN :inicio AND :fim AND a.ativo = true")
    List<Atendimento> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT a FROM Atendimento a WHERE a.colaborador.setor = :setor AND a.dataHora BETWEEN :inicio AND :fim AND a.ativo = true")
    List<Atendimento> findBySetorAndPeriodo(@Param("setor") Colaborador.Setor setor, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(a) FROM Atendimento a WHERE a.tipo = :tipo AND a.ativo = true")
    long countByTipo(@Param("tipo") Atendimento.TipoAtendimento tipo);

    @Query("SELECT COUNT(a) FROM Atendimento a WHERE a.emergencia = true AND a.ativo = true")
    long countEmergencias();

    @Query("SELECT COUNT(a) FROM Atendimento a WHERE a.dataHora BETWEEN :inicio AND :fim AND a.ativo = true")
    long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(a) FROM Atendimento a WHERE a.emergencia = true AND a.dataHora BETWEEN :inicio AND :fim AND a.ativo = true")
    long countEmergenciasByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT a.colaborador.setor, COUNT(a) FROM Atendimento a WHERE a.ativo = true GROUP BY a.colaborador.setor")
    List<Object[]> countBySetorGrouped();

    @Query("SELECT a.tipo, COUNT(a) FROM Atendimento a WHERE a.ativo = true GROUP BY a.tipo")
    List<Object[]> countByTipoGrouped();
}