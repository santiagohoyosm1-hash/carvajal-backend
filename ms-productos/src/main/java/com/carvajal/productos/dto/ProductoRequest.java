package com.carvajal.productos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Cuerpo de POST/PUT. El campo "id" que mande el front se ignora. */
public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 120, message = "El nombre admite hasta 120 caracteres") String nombre,
        @Size(max = 500, message = "La descripcion admite hasta 500 caracteres") String descripcion,
        @NotNull(message = "El precio es obligatorio") @DecimalMin(value = "0.0", message = "El precio no puede ser negativo") BigDecimal precio,
        @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        @Size(max = 80, message = "La categoria admite hasta 80 caracteres") String categoria,
        Boolean activo) { }
