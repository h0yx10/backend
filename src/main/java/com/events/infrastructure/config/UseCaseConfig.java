package com.events.infrastructure.config;

import com.events.application.port.in.ChangeSubtareaStatusPort;
import com.events.application.port.in.CheckOverloadConflictPort;
import com.events.application.port.in.CreateEventoPort;
import com.events.application.port.in.CreateSubtareaPort;
import com.events.application.port.in.DeleteEventoPort;
import com.events.application.port.in.DeleteSubtareaPort;
import com.events.application.port.in.GetCapacidadPort;
import com.events.application.port.in.GetEventoPort;
import com.events.application.port.in.GetEventoProgressPort;
import com.events.application.port.in.GetTodayPort;
import com.events.application.port.in.ListEventosPort;
import com.events.application.port.in.ListSubtareasPort;
import com.events.application.port.in.UpdateCapacidadPort;
import com.events.application.port.in.UpdateEventoPort;
import com.events.application.port.in.UpdateSubtareaPort;
import com.events.application.port.out.CapacidadDiariaRepositoryPort;
import com.events.application.port.out.CurrentOrganizadorPort;
import com.events.application.port.out.EventoRepositoryPort;
import com.events.application.port.out.OrganizadorRepositoryPort;
import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.application.usecase.ChangeSubtareaStatusUseCase;
import com.events.application.usecase.CheckOverloadConflictUseCase;
import com.events.application.usecase.CreateEventoUseCase;
import com.events.application.usecase.CreateSubtareaUseCase;
import com.events.application.usecase.DeleteEventoUseCase;
import com.events.application.usecase.DeleteSubtareaUseCase;
import com.events.application.usecase.GetCapacidadUseCase;
import com.events.application.usecase.GetEventoProgressUseCase;
import com.events.application.usecase.GetEventoUseCase;
import com.events.application.usecase.GetTodayUseCase;
import com.events.application.usecase.ListEventosUseCase;
import com.events.application.usecase.ListSubtareasUseCase;
import com.events.application.usecase.UpdateCapacidadUseCase;
import com.events.application.usecase.UpdateEventoUseCase;
import com.events.application.usecase.UpdateSubtareaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateEventoPort createEventoPort(EventoRepositoryPort eventoRepository,
                                              OrganizadorRepositoryPort organizadorRepository,
                                              CurrentOrganizadorPort currentOrganizador) {
        return new CreateEventoUseCase(eventoRepository, organizadorRepository, currentOrganizador);
    }

    @Bean
    public GetEventoPort getEventoPort(EventoRepositoryPort eventoRepository) {
        return new GetEventoUseCase(eventoRepository);
    }

    @Bean
    public ListEventosPort listEventosPort(EventoRepositoryPort eventoRepository,
                                            CurrentOrganizadorPort currentOrganizador) {
        return new ListEventosUseCase(eventoRepository, currentOrganizador);
    }

    @Bean
    public UpdateEventoPort updateEventoPort(EventoRepositoryPort eventoRepository) {
        return new UpdateEventoUseCase(eventoRepository);
    }

    @Bean
    public DeleteEventoPort deleteEventoPort(EventoRepositoryPort eventoRepository) {
        return new DeleteEventoUseCase(eventoRepository);
    }

    @Bean
    public CreateSubtareaPort createSubtareaPort(EventoRepositoryPort eventoRepository,
                                                  SubtareaRepositoryPort subtareaRepository) {
        return new CreateSubtareaUseCase(eventoRepository, subtareaRepository);
    }

    @Bean
    public ListSubtareasPort listSubtareasPort(EventoRepositoryPort eventoRepository,
                                                SubtareaRepositoryPort subtareaRepository) {
        return new ListSubtareasUseCase(eventoRepository, subtareaRepository);
    }

    @Bean
    public UpdateSubtareaPort updateSubtareaPort(SubtareaRepositoryPort subtareaRepository,
                                                  CapacidadDiariaRepositoryPort capacidadDiariaRepository) {
        return new UpdateSubtareaUseCase(subtareaRepository, capacidadDiariaRepository);
    }

    @Bean
    public ChangeSubtareaStatusPort changeSubtareaStatusPort(SubtareaRepositoryPort subtareaRepository) {
        return new ChangeSubtareaStatusUseCase(subtareaRepository);
    }

    @Bean
    public DeleteSubtareaPort deleteSubtareaPort(SubtareaRepositoryPort subtareaRepository) {
        return new DeleteSubtareaUseCase(subtareaRepository);
    }

    @Bean
    public GetTodayPort getTodayPort(SubtareaRepositoryPort subtareaRepository,
                                      CurrentOrganizadorPort currentOrganizador) {
        return new GetTodayUseCase(subtareaRepository, currentOrganizador);
    }

    @Bean
    public CheckOverloadConflictPort checkOverloadConflictPort(SubtareaRepositoryPort subtareaRepository,
                                                                 CapacidadDiariaRepositoryPort capacidadDiariaRepository) {
        return new CheckOverloadConflictUseCase(subtareaRepository, capacidadDiariaRepository);
    }

    @Bean
    public GetEventoProgressPort getEventoProgressPort(EventoRepositoryPort eventoRepository,
                                                         SubtareaRepositoryPort subtareaRepository) {
        return new GetEventoProgressUseCase(eventoRepository, subtareaRepository);
    }

    @Bean
    public GetCapacidadPort getCapacidadPort(CapacidadDiariaRepositoryPort capacidadDiariaRepository,
                                              CurrentOrganizadorPort currentOrganizador) {
        return new GetCapacidadUseCase(capacidadDiariaRepository, currentOrganizador);
    }

    @Bean
    public UpdateCapacidadPort updateCapacidadPort(CapacidadDiariaRepositoryPort capacidadDiariaRepository,
                                                    OrganizadorRepositoryPort organizadorRepository,
                                                    CurrentOrganizadorPort currentOrganizador) {
        return new UpdateCapacidadUseCase(capacidadDiariaRepository, organizadorRepository, currentOrganizador);
    }
}
