package com.carvajal.productos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

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
    void requiereJwt() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void permitePreflightConHeaderAuthorization() throws Exception {
        mvc.perform(options("/api/productos")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("authorization")));
    }

    @Test
    void flujoCompleto() throws Exception {
        mvc.perform(get("/api/productos").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").exists());

        mvc.perform(post("/api/productos").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists());

        mvc.perform(put("/api/productos/1").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Lampara"));

        mvc.perform(delete("/api/productos/1").with(jwt())).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/1").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void validacionYNoEncontrado() throws Exception {
        mvc.perform(post("/api/productos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"precio\":-1,\"stock\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/productos/9999").with(jwt())).andExpect(status().isNotFound());
    }
}
