package com.industrial.saude.repository;

import com.industrial.saude.model.ProntuarioOcupacional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProntuarioOcupacionalRepository extends JpaRepository<ProntuarioOcupacional, Long> {
    Optional<ProntuarioOcupacional> findByColaboradorId(Long colaboradorId);
}