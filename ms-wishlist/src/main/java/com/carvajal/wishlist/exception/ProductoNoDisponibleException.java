package com.carvajal.wishlist.exception;

public class ProductoNoDisponibleException extends RuntimeException {
    public ProductoNoDisponibleException(Long productoId) {
        super("El producto " + productoId + " no existe o no está activo");
    }
}
