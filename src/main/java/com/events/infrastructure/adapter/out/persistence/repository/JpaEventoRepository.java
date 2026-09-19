package com.events.infrastructure.adapter.out.persistence.repository;

import com.events.domain.entity.Evento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaEventoRepository extends JpaRepository<Evento, UUID> {

    /**
     * Carga el evento con sus subtareas en la misma consulta (LEFT para no perder eventos sin
     * subtareas). Sin el fetch join, EventoRestMapper falla con LazyInitializationException al
     * recorrer evento.getSubtareas() fuera de la sesion de Hibernate que ya se cerro.
     */
    @Query("SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.subtareas WHERE e.id = :id")
    Optional<Evento> findByIdWithSubtareas(@Param("id") UUID id);

    @Query("SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.subtareas WHERE e.organizador.id = :organizadorId")
    List<Evento> findByOrganizadorIdWithSubtareas(@Param("organizadorId") UUID organizadorId);
}
