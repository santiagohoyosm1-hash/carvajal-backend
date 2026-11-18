package com.carvajal.wishlist.exception;

public class DeseoNoEncontradoException extends RuntimeException {
    public DeseoNoEncontradoException(Long id) {
        super("El deseo " + id + " no existe");
    }
}
