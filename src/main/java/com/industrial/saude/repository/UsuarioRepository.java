package com.industrial.saude.repository;

import com.industrial.saude.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u WHERE u.username = :username AND u.ativo = true")
    Optional<Usuario> findAtivoByUsername(String username);
}