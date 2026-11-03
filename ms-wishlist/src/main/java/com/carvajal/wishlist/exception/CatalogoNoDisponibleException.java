package com.carvajal.wishlist.exception;

public class CatalogoNoDisponibleException extends RuntimeException {
    public CatalogoNoDisponibleException(Throwable causa) {
        super("No se pudo consultar el catálogo de productos. Intenta de nuevo en unos minutos", causa);
    }
}
