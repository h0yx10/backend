package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.UsuarioRepositoryPort;
import com.events.domain.entity.Usuario;
import com.events.infrastructure.adapter.out.persistence.repository.JpaUsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;
import com.events.domain.exception.CorreoYaRegistradoException;
import com.events.domain.exception.UsuarioConflictException;

@Repository
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final JpaUsuarioRepository jpaUsuarioRepository;

    public UsuarioPersistenceAdapter(JpaUsuarioRepository jpaUsuarioRepository) {
        this.jpaUsuarioRepository = jpaUsuarioRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        try {
            return jpaUsuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            if (sqlState(ex, "23505"))
                throw new CorreoYaRegistradoException("Ya existe una cuenta con ese correo.");
            throw ex;
        }
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpaUsuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return jpaUsuarioRepository.findByCorreoIgnoreCase(correo);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaUsuarioRepository.existsByCorreoIgnoreCase(correo);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaUsuarioRepository.findAll();
    }
    @Override
    public Optional<Usuario> findByIdForUpdate(UUID id) { return jpaUsuarioRepository.findByIdForUpdate(id); }
    @Override
    public long countActiveAdmins() { return jpaUsuarioRepository.countActiveAdmins(); }
    @Override
    public boolean hasBusinessData(UUID id) { return jpaUsuarioRepository.hasBusinessData(id); }
    @Override
    public void delete(Usuario usuario) {
        try {
            jpaUsuarioRepository.delete(usuario);
            jpaUsuarioRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            if (sqlState(ex, "23503"))
                throw new UsuarioConflictException("No se puede eliminar un usuario con datos asociados.");
            throw ex;
        }
    }
    private static boolean sqlState(Throwable ex, String state) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLException sql && state.equals(sql.getSQLState())) return true;
        }
        return false;
    }
}
