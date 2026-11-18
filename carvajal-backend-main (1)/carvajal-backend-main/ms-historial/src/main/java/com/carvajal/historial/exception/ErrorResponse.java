package com.carvajal.historial.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

/** Formato unico de error (el mismo que ms-wishlist). */
public record ErrorResponse(int status, String error, String mensaje, LocalDateTime fecha) {

    static ErrorResponse de(HttpStatus status, String mensaje) {
        return new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje, LocalDateTime.now());
    }
}
