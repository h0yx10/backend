package com.events.application.port.out;

import com.events.domain.entity.Subtarea;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubtareaRepositoryPort {
    Subtarea save(Subtarea subtarea);

    Optional<Subtarea> findById(UUID id);

    List<Subtarea> findByEventoId(UUID eventoId);

    List<Subtarea> findByOrganizadorId(UUID organizadorId);

    void deleteById(UUID id);

    /**
     * Suma las horas estimadas de subtareas no DONE de un organizador para una fecha,
     * excluyendo (si se indica) la propia subtarea que se esta reprogramando para no duplicar su aporte.
     */
    BigDecimal sumHorasPlanificadas(UUID organizadorId, LocalDate fecha, UUID excludeSubtareaId);
}
