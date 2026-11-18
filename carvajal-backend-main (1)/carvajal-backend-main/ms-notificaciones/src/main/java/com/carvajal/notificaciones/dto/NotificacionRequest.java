package com.carvajal.notificaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificacionRequest(
        @NotNull(message = "El usuario es obligatorio") @Min(value = 1, message = "usuarioId debe ser >= 1") Long usuarioId,
        Long productoId,
        @NotBlank(message = "El mensaje es obligatorio") @Size(max = 500, message = "El mensaje admite hasta 500 caracteres") String mensaje) { }
