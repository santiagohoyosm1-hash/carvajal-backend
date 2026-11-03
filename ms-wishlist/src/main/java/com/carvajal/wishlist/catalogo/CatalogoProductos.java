package com.carvajal.wishlist.catalogo;

import java.util.Optional;

/**
 * Lo que wishlist NECESITA saber del catálogo (interfaz objetivo del patrón Adapter).
 * DeseoService depende de esta interfaz y no sabe si por detrás hay HTTP, caché o un falso de pruebas.
 */
public interface CatalogoProductos {

    /** @return el producto si está disponible en el catálogo; vacío si no existe o está inactivo. */
    Optional<ProductoCatalogo> buscarPorId(Long productoId);
}
