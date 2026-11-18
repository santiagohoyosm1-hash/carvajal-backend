package com.carvajal.wishlist.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final AuthUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminUserInitializer(
            AuthUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${auth.admin.username:}") String username,
            @Value("${auth.admin.password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Configura AUTH_ADMIN_USERNAME y AUTH_ADMIN_PASSWORD");
        }
        AuthService.validarLongitudContrasena(password);
        if (!username.matches("[A-Za-z0-9._-]{3,50}")) {
            throw new IllegalStateException("AUTH_ADMIN_USERNAME debe tener entre 3 y 50 caracteres válidos");
        }
        String normalizedUsername = AuthService.normalizarUsuario(username);
        users.findByUsername(normalizedUsername).ifPresentOrElse(user -> {
            if (!"ADMIN".equals(user.getRole())) {
                throw new IllegalStateException("La cuenta configurada como administrador ya existe sin el rol ADMIN");
            }
        }, () -> users.save(new AuthUser(
                normalizedUsername, passwordEncoder.encode(password), "ADMIN")));
    }
}
