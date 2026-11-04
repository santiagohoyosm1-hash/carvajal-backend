package com.carvajal.notificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.carvajal.contracts.MovimientoRegistradoEvent;
import com.carvajal.notificaciones.messaging.MovimientoRegistradoListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificacionControllerTest {

    @Autowired MockMvc mvc;
    @Autowired MovimientoRegistradoListener movimientoListener;
    @Autowired Jackson2JsonMessageConverter rabbitMessageConverter;

    @Test
    void requiereJwt() throws Exception {
        mvc.perform(get("/api/notificaciones/3")).andExpect(status().isUnauthorized());
    }

    @Test
    void creaListaYMarcaLeida() throws Exception {
        mvc.perform(post("/api/notificaciones").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"productoId\":1,\"mensaje\":\"Hola\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leida").value(false));

        mvc.perform(get("/api/notificaciones/3").with(jwt())).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensaje").value("Hola"));

        mvc.perform(patch("/api/notificaciones/1/leida").with(jwt())).andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void noExiste() throws Exception {
        mvc.perform(patch("/api/notificaciones/999/leida").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void guardaNotificacionAlRecibirMovimiento() throws Exception {
        movimientoListener.recibir(new MovimientoRegistradoEvent(9L, 4L, "Producto agregado"));

        mvc.perform(get("/api/notificaciones/9").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productoId").value(4))
                .andExpect(jsonPath("$[0].mensaje").value("Producto agregado"));
    }

    @Test
    void convierteEventoRabbitAJsonYDeVuelta() {
        MovimientoRegistradoEvent event = new MovimientoRegistradoEvent(9L, 4L, "Producto agregado");

        assertEquals(
                event,
                rabbitMessageConverter.fromMessage(
                        rabbitMessageConverter.toMessage(event, new MessageProperties())));
    }
}
