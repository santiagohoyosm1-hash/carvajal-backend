package com.carvajal.historial;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.carvajal.contracts.MovimientoRegistradoEvent;
import com.carvajal.historial.notificaciones.MovimientoEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HistorialControllerTest {

    @Autowired MockMvc mvc;

    @MockBean MovimientoEventPublisher eventPublisher;

    @Test
    void requiereJwt() throws Exception {
        mvc.perform(get("/api/historial/7")).andExpect(status().isUnauthorized());
    }

    @Test
    void registraYLista() throws Exception {
        mvc.perform(post("/api/historial").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":7,\"productoId\":1,\"accion\":\"agregado\",\"cantidad\":2,\"detalle\":\"Agrego algo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accion").value("AGREGADO"));
        verify(eventPublisher).publicar(
                new MovimientoRegistradoEvent(7L, 1L, "Agrego algo"));

        mvc.perform(get("/api/historial/7").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].detalle").value("Agrego algo"));
    }

    @Test
    void rechazaCuerpoInvalido() throws Exception {
        mvc.perform(post("/api/historial").with(jwt()).contentType(MediaType.APPLICATION_JSON).content("{\"usuarioId\":0}"))
                .andExpect(status().isBadRequest());
    }
}
