package com.carvajal.notificaciones.controller;

import com.carvajal.notificaciones.dto.NotificacionRequest;
import com.carvajal.notificaciones.dto.NotificacionResponse;
import com.carvajal.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping("/{usuarioId}")
    public List<NotificacionResponse> listar(@PathVariable Long usuarioId) {
        return service.listar(usuarioId);
    }

    @PostMapping
    public ResponseEntity<NotificacionResponse> crear(@Valid @RequestBody NotificacionRequest request) {
        NotificacionResponse creada = service.crear(request);
        return ResponseEntity.created(URI.create("/api/notificaciones/" + creada.usuarioId())).body(creada);
    }

    @PatchMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable Long id) {
        return service.marcarLeida(id);
    }
}
