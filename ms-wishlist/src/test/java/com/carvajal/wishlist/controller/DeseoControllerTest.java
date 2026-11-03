package com.carvajal.wishlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carvajal.wishlist.dto.DeseoRequest;
import com.carvajal.wishlist.dto.DeseoResponse;
import com.carvajal.wishlist.exception.DeseoNoEncontradoException;
import com.carvajal.wishlist.exception.StockInsuficienteException;
import com.carvajal.wishlist.service.DeseoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeseoController.class)
class DeseoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DeseoService servicio;

    @Test
    void postValidoDevuelve201() throws Exception {
        when(servicio.agregar(any(DeseoRequest.class))).thenReturn(
                DeseoResponse.builder().id(1L).usuarioId(1L).productoId(2L).cantidadDeseada(3).build());

        mvc.perform(post("/api/wishlist").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"productoId\":2,\"cantidadDeseada\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadDeseada").value(3));
    }

    @Test
    void postSinUsuarioDevuelve400() throws Exception {
        mvc.perform(post("/api/wishlist").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":2,\"cantidadDeseada\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postConCantidadCeroDevuelve400() throws Exception {
        mvc.perform(post("/api/wishlist").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"productoId\":2,\"cantidadDeseada\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchConCantidadCeroDevuelve400() throws Exception {
        mvc.perform(patch("/api/wishlist/7").param("cantidad", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchDeUnDeseoInexistenteDevuelve404() throws Exception {
        when(servicio.actualizar(anyLong(), anyInt())).thenThrow(new DeseoNoEncontradoException(7L));

        mvc.perform(patch("/api/wishlist/7").param("cantidad", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void stockInsuficienteDevuelve409() throws Exception {
        when(servicio.agregar(any(DeseoRequest.class)))
                .thenThrow(new StockInsuficienteException("Cuaderno", 3, 9));

        mvc.perform(post("/api/wishlist").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"productoId\":2,\"cantidadDeseada\":9}"))
                .andExpect(status().isConflict());
    }
}
