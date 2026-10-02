package com.events.application.usecase;

import com.events.application.port.in.AuthResult;
import com.events.application.port.in.RegisterPort;
import com.events.application.port.out.UsuarioRepositoryPort;
import com.events.application.port.out.PasswordHasherPort;
import com.events.application.port.out.RolRepositoryPort;
import com.events.application.port.out.TokenProviderPort;
import com.events.domain.entity.NombreRol;
import com.events.domain.entity.Rol;
import com.events.domain.entity.Usuario;
import com.events.domain.exception.CorreoYaRegistradoException;
import java.util.Locale;

public class RegisterUseCase implements RegisterPort {

    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public RegisterUseCase(UsuarioRepositoryPort usuarioRepository, RolRepositoryPort rolRepository,
                           PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult execute(String nombre, String correo, String password) {
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            throw new CorreoYaRegistradoException("Ya existe una cuenta con ese correo.");
        }

        // Si la tabla roles aun no tiene la semilla (ddl-auto sin docs/schema.sql), se crea al vuelo.
        Rol rolOrganizador = rolRepository.findByNombre(NombreRol.ORGANIZADOR)
                .orElseGet(() -> rolRepository.save(new Rol(NombreRol.ORGANIZADOR)));

        Usuario usuario = new Usuario(nombre.trim(), correoNormalizado, passwordHasher.hash(password));
        usuario.habilitarComoOrganizador().asignarRol(rolOrganizador);
        Usuario guardado = usuarioRepository.save(usuario);

        return new AuthResult(tokenProvider.generate(guardado), tokenProvider.expirationSeconds(), guardado);
    }
}
