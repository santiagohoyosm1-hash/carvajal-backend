package com.carvajal.notificaciones;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void creaListaYMarcaLeida() throws Exception {
        mvc.perform(post("/api/notificaciones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"productoId\":1,\"mensaje\":\"Hola\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leida").value(false));

        mvc.perform(get("/api/notificaciones/3")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensaje").value("Hola"));

        mvc.perform(patch("/api/notificaciones/1/leida")).andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void noExiste() throws Exception {
        mvc.perform(patch("/api/notificaciones/999/leida")).andExpect(status().isNotFound());
    }
}
