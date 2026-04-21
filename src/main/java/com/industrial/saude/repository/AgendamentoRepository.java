package com.industrial.saude.repository;

import com.industrial.saude.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByColaboradorId(Long colaboradorId);
    List<Agendamento> findByStatus(Agendamento.StatusAgendamento status);
    List<Agendamento> findByTipo(Agendamento.TipoExame tipo);
    
    @Query("SELECT a FROM Agendamento a WHERE a.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT a FROM Agendamento a WHERE a.status = 'AGENDADO' AND a.dataHora < :data")
    List<Agendamento> findPendentes(@Param("data") LocalDateTime data);
}