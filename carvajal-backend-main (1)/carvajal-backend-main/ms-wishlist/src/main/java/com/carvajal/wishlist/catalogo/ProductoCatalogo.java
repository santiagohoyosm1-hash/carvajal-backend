package com.carvajal.wishlist.catalogo;

import java.math.BigDecimal;

/**
 * Modelo PROPIO de wishlist para hablar de un producto.
 * Solo tiene lo que este servicio necesita; no copia el formato de ms-productos.
 */
public record ProductoCatalogo(Long id, String nombre, BigDecimal precio, int stock, boolean activo) { }
