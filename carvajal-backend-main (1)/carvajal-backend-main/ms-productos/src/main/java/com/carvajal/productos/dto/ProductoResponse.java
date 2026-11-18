package com.carvajal.productos.dto;

import com.carvajal.productos.entity.Producto;
import java.math.BigDecimal;

public record ProductoResponse(Long id, String nombre, String descripcion, BigDecimal precio,
                               Integer stock, String categoria, boolean activo) {

    public static ProductoResponse de(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getStock(), p.getCategoria(), p.isActivo());
    }
}
