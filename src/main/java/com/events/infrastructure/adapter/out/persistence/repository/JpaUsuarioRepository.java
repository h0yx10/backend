package com.events.infrastructure.adapter.out.persistence.repository;

import com.events.domain.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
