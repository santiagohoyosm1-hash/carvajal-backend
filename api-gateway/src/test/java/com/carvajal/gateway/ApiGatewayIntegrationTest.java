package com.carvajal.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayIntegrationTest {

    private static final String ENCODED_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final SecretKey TEST_KEY = new SecretKeySpec(
            Base64.getDecoder().decode(ENCODED_SECRET), "HmacSHA256");
    private static final ConcurrentLinkedQueue<String> WISHLIST_REQUESTS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> PRODUCT_REQUESTS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> HISTORIAL_REQUESTS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> NOTIFICACIONES_REQUESTS = new ConcurrentLinkedQueue<>();
    private static final HttpServer WISHLIST_SERVER = startServer(WISHLIST_REQUESTS, "wishlist-ok");
    private static final HttpServer PRODUCT_SERVER = startServer(PRODUCT_REQUESTS, "product-ok");
    private static final HttpServer HISTORIAL_SERVER = startServer(HISTORIAL_REQUESTS, "historial-ok");
    private static final HttpServer NOTIFICACIONES_SERVER = startServer(NOTIFICACIONES_REQUESTS, "notificaciones-ok");

    @Autowired WebTestClient webClient;

    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry properties) {
        properties.add("security.jwt.secret", () -> ENCODED_SECRET);
        properties.add("gateway.services.wishlist-url", () -> "http://127.0.0.1:" + WISHLIST_SERVER.getAddress().getPort());
        properties.add("gateway.services.productos-url", () -> "http://127.0.0.1:" + PRODUCT_SERVER.getAddress().getPort());
        properties.add("gateway.services.historial-url", () -> "http://127.0.0.1:" + HISTORIAL_SERVER.getAddress().getPort());
        properties.add("gateway.services.notificaciones-url", () -> "http://127.0.0.1:" + NOTIFICACIONES_SERVER.getAddress().getPort());
    }

    @AfterAll
    static void detenerServiciosDePrueba() {
        WISHLIST_SERVER.stop(0);
        PRODUCT_SERVER.stop(0);
        HISTORIAL_SERVER.stop(0);
        NOTIFICACIONES_SERVER.stop(0);
    }

    @Test
    void permiteLoginPublicoYEnrutaAlServicioWishlist() {
        webClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"admin\",\"password\":\"secret\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("wishlist-ok");

        assertEquals("/api/auth/login|null", WISHLIST_REQUESTS.poll());
    }

    @Test
    void rechazaPeticionesAnonimasEnRutasProtegidas() {
        webClient.get().uri("/api/productos")
                .exchange()
                .expectStatus().isUnauthorized();

        assertEquals(0, PRODUCT_REQUESTS.size());
    }

    @Test
    void permitePreflightCorsConAuthorization() {
        webClient.options().uri("/api/productos")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200")
                .expectHeader().valueMatches(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "(?i).*authorization.*");
    }

    @Test
    void validaJwtYReenviaRutaYBearerTokenAlServicio() {
        String token = crearToken("USER");

        webClient.get().uri("/api/productos/12")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("product-ok");

        assertEquals("/api/productos/12|Bearer " + token, PRODUCT_REQUESTS.poll());
    }

    @Test
    void enrutaHistorialYNotificaciones() {
        String token = crearToken("USER");

        webClient.get().uri("/api/historial/7")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("historial-ok");
        webClient.get().uri("/api/notificaciones/7")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("notificaciones-ok");

        assertEquals("/api/historial/7|Bearer " + token, HISTORIAL_REQUESTS.poll());
        assertEquals("/api/notificaciones/7|Bearer " + token, NOTIFICACIONES_REQUESTS.poll());
    }

    @Test
    void restringeCreacionDeUsuariosAlRolAdmin() {
        webClient.post().uri("/api/auth/users")
                .headers(headers -> headers.setBearerAuth(crearToken("USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"new.user\",\"password\":\"password\"}")
                .exchange()
                .expectStatus().isForbidden();

        String adminToken = crearToken("ADMIN");
        webClient.post().uri("/api/auth/users")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"new.user\",\"password\":\"password\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("wishlist-ok");

        assertEquals("/api/auth/users|Bearer " + adminToken, WISHLIST_REQUESTS.poll());
    }

    private static String crearToken(String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("carvajal-auth")
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .claim("role", role)
                .build();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(TEST_KEY));
        return encoder.encode(JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build(),
                claims)).getTokenValue();
    }

    private static HttpServer startServer(ConcurrentLinkedQueue<String> requests, String responseBody) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                String authorization = exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                requests.add(exchange.getRequestURI().getPath() + "|" + authorization);
                byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                exchange.sendResponseHeaders(200, response.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
