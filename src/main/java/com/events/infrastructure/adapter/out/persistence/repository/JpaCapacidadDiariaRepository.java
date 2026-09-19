package com.events.infrastructure.adapter.out.persistence.repository;

import com.events.domain.entity.CapacidadDiaria;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaCapacidadDiariaRepository extends JpaRepository<CapacidadDiaria, UUID> {

    @Query("SELECT c FROM CapacidadDiaria c WHERE c.organizador.id = :organizadorId ORDER BY c.fecha DESC")
    java.util.List<CapacidadDiaria> findByOrganizadorIdOrderByFechaDesc(@Param("organizadorId") UUID organizadorId);

    default Optional<CapacidadDiaria> findCurrentByOrganizadorId(UUID organizadorId) {
        return findByOrganizadorIdOrderByFechaDesc(organizadorId).stream().findFirst();
    }
}
