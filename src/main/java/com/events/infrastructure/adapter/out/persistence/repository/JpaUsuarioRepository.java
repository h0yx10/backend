package com.events.infrastructure.adapter.out.persistence.repository;

import com.events.domain.entity.Usuario;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface JpaUsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") UUID id);
    @Query("select count(u) from Usuario u join u.roles r left join u.organizador o "
            + "where r.nombre = com.events.domain.entity.NombreRol.ADMIN "
            + "and u.passwordHash is not null and (o is null or o.activo = true)")
    long countActiveAdmins();
    @Query(value = "select exists(select 1 from eventos e join organizadores o on o.id = e.organizador_id "
            + "where o.usuario_id = :id) or exists(select 1 from capacidades_diarias c "
            + "join organizadores o on o.id = c.organizador_id where o.usuario_id = :id)", nativeQuery = true)
    boolean hasBusinessData(@Param("id") UUID id);
}
