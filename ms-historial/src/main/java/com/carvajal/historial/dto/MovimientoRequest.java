package com.carvajal.historial.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
        @NotNull(message = "El usuario es obligatorio") @Min(value = 1, message = "usuarioId debe ser >= 1") Long usuarioId,
        @NotNull(message = "El producto es obligatorio") @Min(value = 1, message = "productoId debe ser >= 1") Long productoId,
        @NotBlank(message = "La accion es obligatoria") @Size(max = 30, message = "La accion admite hasta 30 caracteres") String accion,
        Integer cantidad,
        @Size(max = 500, message = "El detalle admite hasta 500 caracteres") String detalle) { }
