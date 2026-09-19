package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.OrganizadorRepositoryPort;
import com.events.domain.entity.Organizador;
import com.events.infrastructure.adapter.out.persistence.repository.JpaOrganizadorRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizadorPersistenceAdapter implements OrganizadorRepositoryPort {

    private final JpaOrganizadorRepository jpaOrganizadorRepository;

    public OrganizadorPersistenceAdapter(JpaOrganizadorRepository jpaOrganizadorRepository) {
        this.jpaOrganizadorRepository = jpaOrganizadorRepository;
    }

    @Override
    public Organizador save(Organizador organizador) {
        return jpaOrganizadorRepository.save(organizador);
    }

    @Override
    public Optional<Organizador> findById(UUID id) {
        return jpaOrganizadorRepository.findById(id);
    }

    @Override
    public Optional<Organizador> findByCorreo(String correo) {
        return jpaOrganizadorRepository.findByCorreo(correo);
    }
}
