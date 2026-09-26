package com.events.infrastructure.adapter.in.rest.mapper;

import com.events.domain.entity.Subtarea;
import com.events.infrastructure.adapter.in.rest.dto.SubtareaResponse;
import org.springframework.stereotype.Component;

@Component
public class SubtareaRestMapper {

    public SubtareaResponse toResponse(Subtarea subtarea) {
        return new SubtareaResponse(
                subtarea.getId(),
                subtarea.getEvento().getId(),
                subtarea.getNombre(),
                subtarea.getFechaObjetivo(),
                subtarea.getHorasEstimadas(),
                subtarea.getEstado(),
                subtarea.getNota(),
                subtarea.getDoneAt(),
                subtarea.getCreatedAt()
        );
    }
}
