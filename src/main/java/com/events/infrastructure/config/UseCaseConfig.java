package com.events.infrastructure.config;

import com.events.application.port.in.ChangeSubtareaStatusPort;
import com.events.application.port.in.CheckOverloadConflictPort;
import com.events.application.port.in.CreateEventoPort;
import com.events.application.port.in.CreateSubtareaPort;
import com.events.application.port.in.DeleteEventoPort;
import com.events.application.port.in.DeleteSubtareaPort;
import com.events.application.port.in.GetCapacidadPort;
import com.events.application.port.in.GetCurrentUserPort;
import com.events.application.port.in.GetEventoPort;
import com.events.application.port.in.GetEventoProgressPort;
import com.events.application.port.in.GetTodayPort;
import com.events.application.port.in.ListEventosPort;
import com.events.application.port.in.ListSubtareasPort;
import com.events.application.port.in.ListUsuariosPort;
import com.events.application.port.in.LoginPort;
import com.events.application.port.in.RegisterPort;
import com.events.application.port.in.UpdateCapacidadPort;
import com.events.application.port.in.UpdateEventoPort;
import com.events.application.port.in.UpdateSubtareaPort;
import com.events.application.port.out.CapacidadDiariaRepositoryPort;
import com.events.application.port.out.CurrentOrganizadorPort;
import com.events.application.port.out.EventoRepositoryPort;
import com.events.application.port.out.OrganizadorRepositoryPort;
import com.events.application.port.out.PasswordHasherPort;
import com.events.application.port.out.RolRepositoryPort;
import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.application.port.out.TokenProviderPort;
import com.events.application.port.out.UsuarioRepositoryPort;
import com.events.application.usecase.ChangeSubtareaStatusUseCase;
import com.events.application.usecase.CheckOverloadConflictUseCase;
import com.events.application.usecase.CreateEventoUseCase;
import com.events.application.usecase.CreateSubtareaUseCase;
import com.events.application.usecase.DeleteEventoUseCase;
import com.events.application.usecase.DeleteSubtareaUseCase;
import com.events.application.usecase.GetCapacidadUseCase;
import com.events.application.usecase.GetCurrentUserUseCase;
import com.events.application.usecase.GetEventoProgressUseCase;
import com.events.application.usecase.GetEventoUseCase;
import com.events.application.usecase.GetTodayUseCase;
import com.events.application.usecase.ListEventosUseCase;
import com.events.application.usecase.ListSubtareasUseCase;
import com.events.application.usecase.ListUsuariosUseCase;
import com.events.application.usecase.LoginUseCase;
import com.events.application.usecase.RegisterUseCase;
import com.events.application.usecase.UpdateCapacidadUseCase;
import com.events.application.usecase.UpdateEventoUseCase;
import com.events.application.usecase.UpdateSubtareaUseCase;
import com.events.application.port.out.CurrentUsuarioPort;
import com.events.application.port.out.TransactionPort;
import com.events.application.port.in.UsuariosPort;
import com.events.application.usecase.UsuariosUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterPort registerPort(UsuarioRepositoryPort usuarioRepository,
                                     RolRepositoryPort rolRepository,
                                     PasswordHasherPort passwordHasher,
                                     TokenProviderPort tokenProvider, TransactionPort transaction) {
        return new RegisterUseCase(usuarioRepository, rolRepository, passwordHasher, tokenProvider, transaction);
    }

    @Bean
    public LoginPort loginPort(UsuarioRepositoryPort usuarioRepository,
                               PasswordHasherPort passwordHasher,
                               TokenProviderPort tokenProvider) {
        return new LoginUseCase(usuarioRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public GetCurrentUserPort getCurrentUserPort(UsuarioRepositoryPort usuarioRepository,
                                                 CurrentUsuarioPort currentUsuario) {
        return new GetCurrentUserUseCase(usuarioRepository, currentUsuario);
    }

    @Bean
    public UsuariosPort usuariosPort(UsuarioRepositoryPort usuarios, RolRepositoryPort roles,
                                    PasswordHasherPort passwords, CurrentUsuarioPort current, TransactionPort transaction) {
        return new UsuariosUseCase(usuarios, roles, passwords, current, transaction);
    }

    @Bean
    public ListUsuariosPort listUsuariosPort(UsuarioRepositoryPort usuarioRepository) {
        return new ListUsuariosUseCase(usuarioRepository);
    }

    @Bean
    public CreateEventoPort createEventoPort(EventoRepositoryPort eventoRepository,
                                              OrganizadorRepositoryPort organizadorRepository,
                                              CurrentOrganizadorPort currentOrganizador) {
        return new CreateEventoUseCase(eventoRepository, organizadorRepository, currentOrganizador);
    }

    @Bean
    public GetEventoPort getEventoPort(EventoRepositoryPort eventoRepository,
                                       CurrentOrganizadorPort currentOrganizador) {
        return new GetEventoUseCase(eventoRepository, currentOrganizador);
    }

    @Bean
    public ListEventosPort listEventosPort(EventoRepositoryPort eventoRepository,
                                            CurrentOrganizadorPort currentOrganizador) {
        return new ListEventosUseCase(eventoRepository, currentOrganizador);
    }

    @Bean
    public UpdateEventoPort updateEventoPort(EventoRepositoryPort eventoRepository,
                                             CurrentOrganizadorPort currentOrganizador) {
        return new UpdateEventoUseCase(eventoRepository, currentOrganizador);
    }

    @Bean
    public DeleteEventoPort deleteEventoPort(EventoRepositoryPort eventoRepository,
                                             CurrentOrganizadorPort currentOrganizador) {
        return new DeleteEventoUseCase(eventoRepository, currentOrganizador);
    }

    @Bean
    public CreateSubtareaPort createSubtareaPort(EventoRepositoryPort eventoRepository,
                                                  SubtareaRepositoryPort subtareaRepository,
                                                  CurrentOrganizadorPort currentOrganizador) {
        return new CreateSubtareaUseCase(eventoRepository, subtareaRepository, currentOrganizador);
    }

    @Bean
    public ListSubtareasPort listSubtareasPort(EventoRepositoryPort eventoRepository,
                                                SubtareaRepositoryPort subtareaRepository,
                                                CurrentOrganizadorPort currentOrganizador) {
        return new ListSubtareasUseCase(eventoRepository, subtareaRepository, currentOrganizador);
    }

    @Bean
    public UpdateSubtareaPort updateSubtareaPort(SubtareaRepositoryPort subtareaRepository,
                                                  CapacidadDiariaRepositoryPort capacidadDiariaRepository,
                                                  CurrentOrganizadorPort currentOrganizador) {
        return new UpdateSubtareaUseCase(subtareaRepository, capacidadDiariaRepository, currentOrganizador);
    }

    @Bean
    public ChangeSubtareaStatusPort changeSubtareaStatusPort(SubtareaRepositoryPort subtareaRepository,
                                                              CurrentOrganizadorPort currentOrganizador) {
        return new ChangeSubtareaStatusUseCase(subtareaRepository, currentOrganizador);
    }

    @Bean
    public DeleteSubtareaPort deleteSubtareaPort(SubtareaRepositoryPort subtareaRepository,
                                                 CurrentOrganizadorPort currentOrganizador) {
        return new DeleteSubtareaUseCase(subtareaRepository, currentOrganizador);
    }

    @Bean
    public GetTodayPort getTodayPort(SubtareaRepositoryPort subtareaRepository,
                                      CurrentOrganizadorPort currentOrganizador) {
        return new GetTodayUseCase(subtareaRepository, currentOrganizador);
    }

    @Bean
    public CheckOverloadConflictPort checkOverloadConflictPort(SubtareaRepositoryPort subtareaRepository,
                                                                 CapacidadDiariaRepositoryPort capacidadDiariaRepository,
                                                                 CurrentOrganizadorPort currentOrganizador) {
        return new CheckOverloadConflictUseCase(subtareaRepository, capacidadDiariaRepository, currentOrganizador);
    }

    @Bean
    public GetEventoProgressPort getEventoProgressPort(EventoRepositoryPort eventoRepository,
                                                         SubtareaRepositoryPort subtareaRepository,
                                                         CurrentOrganizadorPort currentOrganizador) {
        return new GetEventoProgressUseCase(eventoRepository, subtareaRepository, currentOrganizador);
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
