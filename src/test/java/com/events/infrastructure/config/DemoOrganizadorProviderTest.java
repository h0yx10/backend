package com.events.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.events.application.port.out.OrganizadorRepositoryPort;
import com.events.domain.entity.Organizador;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DemoOrganizadorProviderTest {

    private final OrganizadorRepositoryPort organizadorRepository = mock(OrganizadorRepositoryPort.class);
    private final DemoOrganizadorProvider provider = new DemoOrganizadorProvider(organizadorRepository);

    @Test
    void creaElOrganizadorDemoSoloUnaVez() {
        // El id solo lo asigna JPA al persistir; en el test se simula con un mock ya que
        // el constructor de dominio no lo expone (se genera por reflexion via @GeneratedValue).
        Organizador demo = mock(Organizador.class);
        when(demo.getId()).thenReturn(UUID.randomUUID());
        when(organizadorRepository.findByCorreo("demo@organizador.local")).thenReturn(Optional.empty());
        when(organizadorRepository.save(any())).thenReturn(demo);

        var primeraLlamada = provider.currentOrganizadorId();
        var segundaLlamada = provider.currentOrganizadorId();

        assertThat(primeraLlamada).isEqualTo(segundaLlamada);
        verify(organizadorRepository, times(1)).findByCorreo(any());
    }
}
