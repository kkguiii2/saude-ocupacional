package com.industrial.saude.repository;

import com.industrial.saude.model.AuditoriaLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {
    List<AuditoriaLog> findByUsuarioId(Long usuarioId);
    List<AuditoriaLog> findByModulo(String modulo);
    List<AuditoriaLog> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM AuditoriaLog a WHERE " +
           "(:modulo IS NULL OR a.modulo = :modulo) AND " +
           "(:usuarioId IS NULL OR a.usuario.id = :usuarioId) AND " +
           "(:acao IS NULL OR a.acao = :acao) AND " +
           "(:inicio IS NULL OR a.dataHora >= :inicio) AND " +
           "(:fim IS NULL OR a.dataHora <= :fim) " +
           "ORDER BY a.dataHora DESC")
    List<AuditoriaLog> findByFiltros(
            @Param("modulo") String modulo,
            @Param("usuarioId") Long usuarioId,
            @Param("acao") String acao,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    Page<AuditoriaLog> findByOrderByDataHoraDesc(Pageable pageable);
}