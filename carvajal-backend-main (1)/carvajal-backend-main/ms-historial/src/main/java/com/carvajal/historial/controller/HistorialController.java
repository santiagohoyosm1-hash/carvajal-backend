package com.carvajal.historial.controller;

import com.carvajal.historial.dto.MovimientoRequest;
import com.carvajal.historial.dto.MovimientoResponse;
import com.carvajal.historial.service.HistorialService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final HistorialService service;

    public HistorialController(HistorialService service) {
        this.service = service;
    }

    @GetMapping("/{usuarioId}")
    public List<MovimientoResponse> listar(@PathVariable Long usuarioId) {
        return service.listar(usuarioId);
    }

    @PostMapping
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        MovimientoResponse creado = service.registrar(request);
        return ResponseEntity.created(URI.create("/api/historial/" + creado.usuarioId())).body(creado);
    }
}
