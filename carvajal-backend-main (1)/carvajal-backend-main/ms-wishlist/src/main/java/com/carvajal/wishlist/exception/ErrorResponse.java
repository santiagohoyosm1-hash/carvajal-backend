package com.carvajal.wishlist.exception;

import java.time.LocalDateTime;

/** Formato único de error para toda la API. */
public record ErrorResponse(int status, String error, String mensaje, LocalDateTime fecha) {

    static ErrorResponse de(org.springframework.http.HttpStatus status, String mensaje) {
        return new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje, LocalDateTime.now());
    }
}
