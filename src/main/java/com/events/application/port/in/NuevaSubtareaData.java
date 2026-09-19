package com.events.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NuevaSubtareaData(String nombre, LocalDate fechaObjetivo, BigDecimal horasEstimadas) {
}
