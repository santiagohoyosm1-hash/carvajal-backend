package com.carvajal.wishlist.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;

import com.carvajal.wishlist.exception.CatalogoNoDisponibleException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ProductosHttpAdapterTest {

    private static final String LISTA = """
            [{"id":1,"nombre":"Cuaderno","descripcion":"100 hojas","precio":12500.00,
              "stock":20,"categoria":"Papelería","activo":true},
             {"id":4,"nombre":"Mochila","descripcion":null,"precio":89900.00,
              "stock":null,"categoria":"Accesorios","activo":true}]
            """;

    private MockRestServiceServer servidor;
    private ProductosHttpAdapter adaptador;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://productos");
        servidor = MockRestServiceServer.bindTo(builder).build();
        adaptador = new ProductosHttpAdapter(builder.build());
    }

    @AfterEach
    void limpiarSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void traduceElProductoAlModeloPropio() {
        servidor.expect(requestTo("http://productos/api/productos"))
                .andRespond(withSuccess(LISTA, MediaType.APPLICATION_JSON));

        Optional<ProductoCatalogo> resultado = adaptador.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Cuaderno", resultado.get().nombre());
        assertEquals(20, resultado.get().stock());
        assertTrue(resultado.get().activo());
    }

    @Test
    void stockNuloSeTraduceACero() {
        servidor.expect(requestTo("http://productos/api/productos"))
                .andRespond(withSuccess(LISTA, MediaType.APPLICATION_JSON));

        assertEquals(0, adaptador.buscarPorId(4L).orElseThrow().stock());
    }

    @Test
    void devuelveVacioSiElProductoNoEstaEnElCatalogo() {
        servidor.expect(requestTo("http://productos/api/productos"))
                .andRespond(withSuccess(LISTA, MediaType.APPLICATION_JSON));

        assertTrue(adaptador.buscarPorId(99L).isEmpty());
    }

    @Test
    void lanzaCatalogoNoDisponibleSiMsProductosFalla() {
        servidor.expect(requestTo("http://productos/api/productos"))
                .andRespond(withServerError());

        assertThrows(CatalogoNoDisponibleException.class, () -> adaptador.buscarPorId(1L));
    }

    @Test
    void propagaElJwtEnLaLlamadaAlCatalogo() {
        String tokenValue = "access-token";
        Jwt token = Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .subject("7")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
        servidor.expect(requestTo("http://productos/api/productos"))
                .andExpect(header("Authorization", "Bearer " + tokenValue))
                .andRespond(withSuccess(LISTA, MediaType.APPLICATION_JSON));

        assertTrue(adaptador.buscarPorId(1L).isPresent());
    }
}
