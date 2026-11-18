package com.carvajal.wishlist.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** Manejo global de errores: cada excepción de negocio se traduce a su código HTTP. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DeseoNoEncontradoException.class, ProductoNoDisponibleException.class})
    public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> conflicto(StockInsuficienteException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(CatalogoNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> catalogoCaido(CatalogoNoDisponibleException e) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    /** Dos peticiones simultáneas con el mismo usuario+producto chocan con la restricción única. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT, "Ese producto ya está en la lista de deseos del usuario");
    }

    /** Falla @Valid sobre el cuerpo (DeseoRequest). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensaje);
    }

    /** Falla @Min sobre un parámetro (PATCH ?cantidad=0). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(HandlerMethodValidationException e) {
        String mensaje = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> violacion(ConstraintViolationException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(ErrorResponse.de(status, mensaje));
    }
}
