package com.industrial.saude.repository;

import com.industrial.saude.model.Colaborador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    Optional<Colaborador> findByMatricula(String matricula);

    Page<Colaborador> findByAtivoTrue(Pageable pageable);

    List<Colaborador> findByAtivoTrue();

    List<Colaborador> findBySetorAndAtivoTrue(Colaborador.Setor setor);

    List<Colaborador> findBySetor(Colaborador.Setor setor);

    List<Colaborador> findByTipoRiscoAndAtivoTrue(Colaborador.TipoRisco tipoRisco);

    List<Colaborador> findByStatusFuncionarioAndAtivoTrue(Colaborador.StatusFuncionario status);

    @Query("SELECT c FROM Colaborador c WHERE c.setor = :setor AND c.ativo = true")
    List<Colaborador> findAtivosBySetor(@Param("setor") Colaborador.Setor setor);

    @Query("SELECT c FROM Colaborador c WHERE c.statusFuncionario = :status AND c.ativo = true")
    List<Colaborador> findByStatus(@Param("status") Colaborador.StatusFuncionario status);

    @Query("SELECT COUNT(c) FROM Colaborador c WHERE c.setor = :setor AND c.ativo = true")
    long countBySetor(@Param("setor") Colaborador.Setor setor);

    @Query("SELECT COUNT(c) FROM Colaborador c WHERE c.ativo = true")
    long countAtivos();

    @Query("SELECT COUNT(c) FROM Colaborador c WHERE c.statusFuncionario = :status AND c.ativo = true")
    long countByStatus(@Param("status") Colaborador.StatusFuncionario status);

    @Query("SELECT c.setor, COUNT(c) FROM Colaborador c WHERE c.ativo = true GROUP BY c.setor")
    List<Object[]> countGroupBySetor();

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);
}