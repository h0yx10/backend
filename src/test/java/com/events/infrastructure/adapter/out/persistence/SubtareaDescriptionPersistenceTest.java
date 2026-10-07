package com.events.infrastructure.adapter.out.persistence;

import com.events.domain.entity.*;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class SubtareaDescriptionPersistenceTest {
    @Autowired TestEntityManager entities;
    @Test
    void guardaDescriptionEnColumnaDescripcionYLaRecupera() {
        var usuario = new Usuario("Camila", "camila@correo.com", "hash");
        var organizador = usuario.habilitarComoOrganizador();
        entities.persistAndFlush(usuario);
        var evento = entities.persistAndFlush(new Evento("Boda", "Social", null, null, LocalDateTime.now(), null, null, organizador));
        var subtarea = new Subtarea("Catering", "a".repeat(255), LocalDate.now(), BigDecimal.ONE);
        subtarea.asociarEvento(evento);
        entities.persistAndFlush(subtarea);
        var id = subtarea.getId();
        assertThat(entities.getEntityManager().createNativeQuery("select descripcion from subtareas where id = :id")
                .setParameter("id", id).getSingleResult()).isEqualTo("a".repeat(255));
        entities.clear();
        var recuperada = entities.find(Subtarea.class, id);
        assertThat(recuperada.getDescripcion()).isEqualTo("a".repeat(255));
        recuperada.actualizar(null, "Descripcion editada", null, null);
        entities.flush();
        entities.clear();
        assertThat(entities.find(Subtarea.class, id).getDescripcion()).isEqualTo("Descripcion editada");
    }
}
