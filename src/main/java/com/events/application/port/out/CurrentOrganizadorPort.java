package com.events.application.port.out;

import java.util.UUID;

/**
 * Resuelve el organizador "actual". Mientras no exista autenticacion (US-11, Sprint 2+),
 * siempre resuelve al organizador demo (Sprint 0-1). Este puerto es el punto de extension
 * para reemplazar la resolucion por el usuario autenticado en una fase posterior.
 */
public interface CurrentOrganizadorPort {
    UUID currentOrganizadorId();
}
