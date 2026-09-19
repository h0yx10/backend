package com.events.infrastructure.adapter.out.persistence.repository;

import com.events.domain.entity.Subtarea;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaSubtareaRepository extends JpaRepository<Subtarea, UUID> {

    List<Subtarea> findByEventoId(UUID eventoId);

    /**
     * Carga la subtarea junto con evento y organizador en la misma consulta. Los casos de uso
     * que reprograman/validan sobrecarga navegan subtarea -> evento -> organizador; sin este
     * fetch join esa navegacion falla con LazyInitializationException porque la sesion de
     * Hibernate ya se cerro cuando el puerto de salida retorna.
     */
    @Query("SELECT s FROM Subtarea s JOIN FETCH s.evento e JOIN FETCH e.organizador WHERE s.id = :id")
    Optional<Subtarea> findByIdWithEventoYOrganizador(@Param("id") UUID id);

    @Query("SELECT s FROM Subtarea s WHERE s.evento.organizador.id = :organizadorId")
    List<Subtarea> findByOrganizadorId(@Param("organizadorId") UUID organizadorId);

    @Query("""
            SELECT COALESCE(SUM(s.horasEstimadas), 0) FROM Subtarea s
            WHERE s.evento.organizador.id = :organizadorId
              AND s.fechaObjetivo = :fecha
              AND s.estado <> com.events.domain.entity.EstadoSubtarea.DONE
              AND (:excludeId IS NULL OR s.id <> :excludeId)
            """)
    BigDecimal sumHorasPlanificadas(@Param("organizadorId") UUID organizadorId,
                                     @Param("fecha") LocalDate fecha,
                                     @Param("excludeId") UUID excludeId);
}
