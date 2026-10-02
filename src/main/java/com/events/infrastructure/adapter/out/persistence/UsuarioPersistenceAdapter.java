package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.UsuarioRepositoryPort;
import com.events.domain.entity.Usuario;
import com.events.infrastructure.adapter.out.persistence.repository.JpaUsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final JpaUsuarioRepository jpaUsuarioRepository;

    public UsuarioPersistenceAdapter(JpaUsuarioRepository jpaUsuarioRepository) {
        this.jpaUsuarioRepository = jpaUsuarioRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        return jpaUsuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpaUsuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return jpaUsuarioRepository.findByCorreo(correo);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaUsuarioRepository.existsByCorreo(correo);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaUsuarioRepository.findAll();
    }
}
