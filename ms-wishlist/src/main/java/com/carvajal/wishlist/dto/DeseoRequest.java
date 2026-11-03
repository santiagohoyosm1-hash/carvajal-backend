package com.carvajal.wishlist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Lo que el cliente envía al crear un deseo. Si no manda cantidad, se asume 1. */
public record DeseoRequest(
        @NotNull(message = "usuarioId es obligatorio") Long usuarioId,
        @NotNull(message = "productoId es obligatorio") Long productoId,
        @Min(value = 1, message = "cantidadDeseada debe ser al menos 1") Integer cantidadDeseada) { }
