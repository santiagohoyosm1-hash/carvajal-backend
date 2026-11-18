package com.carvajal.wishlist.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String nombre, int stock, int pedido) {
        super("Solo hay " + stock + " unidades de \"" + nombre + "\" y pediste " + pedido);
    }
}
