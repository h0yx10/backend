package com.events.infrastructure.config;

import com.events.application.port.out.CurrentOrganizadorPort;
import com.events.application.port.out.OrganizadorRepositoryPort;
import com.events.domain.entity.Organizador;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * Resuelve siempre al mismo organizador "demo" (Sprint 0-1, sin login). Lo crea de forma
 * perezosa e idempotente la primera vez que se necesita. Cuando se implemente US-11/TS-04,
 * este componente se reemplaza por uno que lea el organizador desde el contexto de seguridad.
 */
@Component
public class DemoOrganizadorProvider implements CurrentOrganizadorPort {

    private static final String DEMO_CORREO = "demo@organizador.local";
    private static final String DEMO_NOMBRE = "Organizador Demo";

    private final OrganizadorRepositoryPort organizadorRepository;
    private final AtomicReference<UUID> cachedId = new AtomicReference<>();

    public DemoOrganizadorProvider(OrganizadorRepositoryPort organizadorRepository) {
        this.organizadorRepository = organizadorRepository;
    }

    @Override
    public synchronized UUID currentOrganizadorId() {
        UUID cached = cachedId.get();
        if (cached != null) {
            return cached;
        }
        Organizador organizador = organizadorRepository.findByCorreo(DEMO_CORREO)
                .orElseGet(() -> organizadorRepository.save(new Organizador(DEMO_NOMBRE, DEMO_CORREO)));
        cachedId.set(organizador.getId());
        return organizador.getId();
    }
}
