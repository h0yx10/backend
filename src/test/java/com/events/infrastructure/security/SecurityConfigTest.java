package com.events.infrastructure.security;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.events.application.port.in.AuthResult;
import com.events.application.port.in.GetCurrentUserPort;
import com.events.application.port.in.ListEventosPort;
import com.events.application.port.in.ListUsuariosPort;
import com.events.application.port.in.LoginPort;
import com.events.application.port.in.RegisterPort;
import com.events.application.port.out.CurrentOrganizadorPort;
import com.events.domain.entity.NombreRol;
import com.events.domain.entity.Usuario;
import com.events.domain.entity.Rol;
import com.events.infrastructure.adapter.in.rest.controller.AdminController;
import com.events.infrastructure.adapter.in.rest.controller.AuthController;
import com.events.infrastructure.adapter.in.rest.controller.EventoController;
import com.events.infrastructure.adapter.in.rest.exception.GlobalExceptionHandler;
import com.events.infrastructure.adapter.in.rest.mapper.EventoRestMapper;
import com.events.infrastructure.adapter.in.rest.mapper.SubtareaRestMapper;
import com.events.infrastructure.adapter.in.rest.mapper.UsuarioRestMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Valida las rutas protegidas end-to-end a nivel web: sin token -> 401, token invalido -> 401,
 * rol insuficiente -> 403, token valido -> 200, y rutas publicas accesibles sin token.
 */
@WebMvcTest(controllers = {AuthController.class, EventoController.class, AdminController.class})
@Import({SecurityConfig.class, RestAuthenticationErrorHandler.class, JwtTokenProviderAdapter.class,
        SecurityContextCurrentOrganizadorAdapter.class, GlobalExceptionHandler.class,
        UsuarioRestMapper.class, EventoRestMapper.class, SubtareaRestMapper.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-secret-test-secret-test-secret-123",
        "app.security.jwt.issuer=events-api",
        "app.security.jwt.expiration-minutes=60"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProviderAdapter tokenProvider;
    @Autowired
    private CurrentOrganizadorPort currentOrganizador;

    @MockBean
    private RegisterPort registerPort;
    @MockBean
    private LoginPort loginPort;
    @MockBean
    private GetCurrentUserPort getCurrentUserPort;
    @MockBean
    private ListUsuariosPort listUsuariosPort;
    @MockBean
    private ListEventosPort listEventosPort;
    @MockBean
    private com.events.application.port.in.CreateEventoPort createEventoPort;
    @MockBean
    private com.events.application.port.in.GetEventoPort getEventoPort;
    @MockBean
    private com.events.application.port.in.UpdateEventoPort updateEventoPort;
    @MockBean
    private com.events.application.port.in.DeleteEventoPort deleteEventoPort;
    @MockBean
    private com.events.application.port.in.GetEventoProgressPort getEventoProgressPort;

    private Usuario usuarioCon(NombreRol... roles) {
        Usuario organizador = new Usuario("Camila", "camila@correo.com", "hash");
        ReflectionTestUtils.setField(organizador, "id", UUID.randomUUID());
        var perfil = organizador.habilitarComoOrganizador();
        for (NombreRol rol : roles) {
            perfil.asignarRol(new Rol(rol));
        }
        return organizador;
    }

    private String bearer(Usuario organizador) {
        return "Bearer " + tokenProvider.generate(organizador);
    }

    @Test
    void rutaProtegidaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(RestAuthenticationErrorHandler.TOKEN_REQUIRED));
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/events").header("Authorization", "Bearer no-es-un-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(RestAuthenticationErrorHandler.TOKEN_INVALID));
    }

    @Test
    void tokenValidoPermiteAccederYExponeElUsuarioActual() throws Exception {
        Usuario usuario = usuarioCon(NombreRol.ORGANIZADOR);
        when(listEventosPort.execute()).thenAnswer(invocation -> {
            // El adaptador resuelve el "sub" del JWT como organizador actual.
            org.assertj.core.api.Assertions.assertThat(currentOrganizador.currentOrganizadorId()).isEqualTo(usuario.getId());
            return List.of();
        });

        mockMvc.perform(get("/api/events").header("Authorization", bearer(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rutaAdminConRolOrganizadorDevuelve403() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(usuarioCon(NombreRol.ORGANIZADOR))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(RestAuthenticationErrorHandler.ACCESS_DENIED));
    }

    @Test
    void rutaAdminConRolAdminDevuelve200() throws Exception {
        when(listUsuariosPort.execute()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(usuarioCon(NombreRol.ADMIN))))
                .andExpect(status().isOk());
    }

    @Test
    void loginEsPublico() throws Exception {
        Usuario usuario = usuarioCon(NombreRol.ORGANIZADOR);
        when(loginPort.execute(any(), any())).thenReturn(new AuthResult("jwt", 3600, usuario));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"camila@correo.com\",\"password\":\"Secreta123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.usuario.roles[0]").value("ORGANIZADOR"));
    }

    @Test
    void registroValidaElBody() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Camila\",\"correo\":\"no-es-correo\",\"password\":\"Secreta123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("correo")));
    }
}
