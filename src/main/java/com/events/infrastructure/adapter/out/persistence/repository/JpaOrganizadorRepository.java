package com.events.infrastructure.adapter.out.persistence.repository;

import com.events.domain.entity.Organizador;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrganizadorRepository extends JpaRepository<Organizador, UUID> {
    Optional<Organizador> findByCorreo(String correo);
}
