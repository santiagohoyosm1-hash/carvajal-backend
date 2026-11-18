package com.carvajal.wishlist.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** Lo que la API devuelve (patrón Builder con Lombok, sección 3.3 de la guía). */
@Getter
@Builder
public class DeseoResponse {
    private Long id;
    private Long usuarioId;
    private Long productoId;
    private Integer cantidadDeseada;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
