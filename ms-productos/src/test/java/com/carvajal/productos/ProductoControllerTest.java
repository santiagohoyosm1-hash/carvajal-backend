package com.carvajal.productos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class ProductoControllerTest {

    @Autowired MockMvc mvc;

    private static final String JSON = "{\"nombre\":\"Lampara\",\"descripcion\":null,\"precio\":15000,\"stock\":5,\"categoria\":\"Hogar\",\"activo\":true}";

    @Test
    void flujoCompleto() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").exists());

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists());

        mvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Lampara"));

        mvc.perform(delete("/api/productos/1")).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void validacionYNoEncontrado() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"precio\":-1,\"stock\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/productos/9999")).andExpect(status().isNotFound());
    }
}
