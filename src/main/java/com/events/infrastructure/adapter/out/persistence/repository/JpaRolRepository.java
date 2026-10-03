package com.events.infrastructure.adapter.out.persistence.repository;

import com.events.domain.entity.NombreRol;
import com.events.domain.entity.Rol;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRolRepository extends JpaRepository<Rol, UUID> {
    Optional<Rol> findByNombre(NombreRol nombre);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from Rol r where r.nombre = com.events.domain.entity.NombreRol.ADMIN")
    Optional<Rol> lockAdminGuard();
}
