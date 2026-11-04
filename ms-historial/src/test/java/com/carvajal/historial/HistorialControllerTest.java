package com.carvajal.historial;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class HistorialControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void registraYLista() throws Exception {
        mvc.perform(post("/api/historial").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":7,\"productoId\":1,\"accion\":\"agregado\",\"cantidad\":2,\"detalle\":\"Agrego algo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accion").value("AGREGADO"));

        mvc.perform(get("/api/historial/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].detalle").value("Agrego algo"));
    }

    @Test
    void rechazaCuerpoInvalido() throws Exception {
        mvc.perform(post("/api/historial").contentType(MediaType.APPLICATION_JSON).content("{\"usuarioId\":0}"))
                .andExpect(status().isBadRequest());
    }
}
