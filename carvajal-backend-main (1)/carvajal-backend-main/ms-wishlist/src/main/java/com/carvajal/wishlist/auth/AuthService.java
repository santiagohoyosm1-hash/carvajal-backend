package com.carvajal.wishlist.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;

    private final AuthUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Duration accessTokenTtl;

    public AuthService(
            AuthUserRepository users,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtl = accessTokenTtl;
        if (accessTokenTtl.compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalArgumentException("La duración del JWT debe ser de al menos un segundo");
        }
    }

    @Transactional
    public AuthUserResponse crearUsuario(CreateUserRequest request) {
        String username = normalizarUsuario(request.username());
        validarLongitudContrasena(request.password());
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }
        AuthUser user = users.save(new AuthUser(
                username, passwordEncoder.encode(request.password()), "USER"));
        return AuthUserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest request) {
        String username = normalizarUsuario(request.username());
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
        AuthUser user = users.findByUsername(username)
                .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessTokenTtl);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("carvajal-auth")
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", user.getRole())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", accessTokenTtl.toSeconds(), user.getId(), user.getRole());
    }

    static void validarLongitudContrasena(String password) {
        int bytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < 12 || bytes > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La contraseña debe ocupar entre 12 y 72 bytes UTF-8");
        }
    }

    static String normalizarUsuario(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
