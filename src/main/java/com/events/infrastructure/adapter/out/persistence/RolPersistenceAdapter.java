package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.RolRepositoryPort;
import com.events.domain.entity.NombreRol;
import com.events.domain.entity.Rol;
import com.events.infrastructure.adapter.out.persistence.repository.JpaRolRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RolPersistenceAdapter implements RolRepositoryPort {

    private final JpaRolRepository jpaRolRepository;

    public RolPersistenceAdapter(JpaRolRepository jpaRolRepository) {
        this.jpaRolRepository = jpaRolRepository;
    }

    @Override
    public Optional<Rol> findByNombre(NombreRol nombre) {
        return jpaRolRepository.findByNombre(nombre);
    }

    @Override
    public Rol save(Rol rol) {
        return jpaRolRepository.save(rol);
    }
}
