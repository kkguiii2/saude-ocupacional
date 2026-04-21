package com.industrial.saude.repository;

import com.industrial.saude.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findByMedicamentoId(Long medicamentoId);
    List<MovimentacaoEstoque> findByTipo(MovimentacaoEstoque.TipoMovimentacao tipo);
}