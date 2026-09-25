package com.events.application.usecase;

import com.events.application.port.in.UpdateSubtareaPort;
import com.events.application.port.out.CapacidadDiariaRepositoryPort;
import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.domain.entity.CapacidadDiaria;
import com.events.domain.entity.Subtarea;
import com.events.domain.exception.CapacityConflictException;
import com.events.domain.exception.SubtareaNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class UpdateSubtareaUseCase implements UpdateSubtareaPort {

    private final SubtareaRepositoryPort subtareaRepository;
    private final CapacidadDiariaRepositoryPort capacidadDiariaRepository;

    public UpdateSubtareaUseCase(SubtareaRepositoryPort subtareaRepository,
                                  CapacidadDiariaRepositoryPort capacidadDiariaRepository) {
        this.subtareaRepository = subtareaRepository;
        this.capacidadDiariaRepository = capacidadDiariaRepository;
    }

    @Override
    public Subtarea execute(UUID subtareaId, String nombre, LocalDate fechaObjetivo, BigDecimal horasEstimadas, String descripcion) {
        Subtarea subtarea = subtareaRepository.findById(subtareaId)
                .orElseThrow(() -> new SubtareaNotFoundException("No encontramos la subtarea solicitada."));

        boolean cambiaCarga = fechaObjetivo != null || horasEstimadas != null;
        if (cambiaCarga) {
            LocalDate fechaEfectiva = fechaObjetivo != null ? fechaObjetivo : subtarea.getFechaObjetivo();
            BigDecimal horasEfectivas = horasEstimadas != null ? horasEstimadas : subtarea.getHorasEstimadas();
            UUID organizadorId = subtarea.getEvento().getOrganizador().getId();

            BigDecimal limite = capacidadDiariaRepository.findCurrentByOrganizadorId(organizadorId)
                    .map(CapacidadDiaria::getLimiteHoras)
                    .orElse(CapacidadDiaria.LIMITE_POR_DEFECTO);
            BigDecimal otrasHoras = subtareaRepository.sumHorasPlanificadas(organizadorId, fechaEfectiva, subtarea.getId());
            BigDecimal totalPlanificado = otrasHoras.add(horasEfectivas);

            if (totalPlanificado.compareTo(limite) > 0) {
                throw new CapacityConflictException(totalPlanificado, limite);
            }
        }

        subtarea.actualizar(nombre, fechaObjetivo, horasEstimadas, descripcion);
        return subtareaRepository.save(subtarea);
    }
}
