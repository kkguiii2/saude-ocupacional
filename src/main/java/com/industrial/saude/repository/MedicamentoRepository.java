package com.industrial.saude.repository;

import com.industrial.saude.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    List<Medicamento> findByAtivo(boolean ativo);
    List<Medicamento> findByCategoria(Medicamento.CategoriaMedicamento categoria);
    
    @Query("SELECT m FROM Medicamento m WHERE m.quantidadeEstoque <= m.quantidadeMinima")
    List<Medicamento> findEstoqueBaixo();
    
    @Query("SELECT COUNT(m) FROM Medicamento m WHERE m.quantidadeEstoque <= m.quantidadeMinima")
    long countEstoqueBaixo();

    @Query("SELECT m FROM Medicamento m WHERE m.ativo = true AND m.dataValidade IS NOT NULL AND m.dataValidade <= :dataLimite AND m.dataValidade > CURRENT_TIMESTAMP")
    List<Medicamento> findPrestesAVencer(@Param("dataLimite") java.time.LocalDateTime dataLimite);
    
    @Query("SELECT m FROM Medicamento m WHERE m.ativo = true AND m.dataValidade IS NOT NULL AND m.dataValidade <= CURRENT_TIMESTAMP")
    List<Medicamento> findVencidos();
}