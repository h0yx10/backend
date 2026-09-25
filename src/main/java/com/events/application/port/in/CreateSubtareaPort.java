package com.events.application.port.in;

import com.events.domain.entity.Subtarea;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreateSubtareaPort {
    Subtarea execute(UUID eventoId, String nombre, LocalDate fechaObjetivo, BigDecimal horasEstimadas, String descripcion);
}
