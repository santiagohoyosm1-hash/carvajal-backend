package com.carvajal.wishlist.controller;

import com.carvajal.wishlist.dto.DeseoRequest;
import com.carvajal.wishlist.dto.DeseoResponse;
import com.carvajal.wishlist.service.DeseoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Controlador delgado: recibe, delega en el servicio y responde. Sin lógica de negocio. */
@RestController
@RequestMapping("/api/wishlist")
public class DeseoController {

    private final DeseoService servicio;

    public DeseoController(DeseoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{usuarioId}")
    public List<DeseoResponse> listar(@PathVariable Long usuarioId) {
        return servicio.listar(usuarioId);
    }

    @PostMapping
    public ResponseEntity<DeseoResponse> agregar(@Valid @RequestBody DeseoRequest request) {
        DeseoResponse creado = servicio.agregar(request);
        return ResponseEntity
                .created(URI.create("/api/wishlist/" + creado.getUsuarioId()))
                .body(creado);
    }

    @PatchMapping("/{id}")
    public DeseoResponse actualizar(@PathVariable Long id,
                                    @RequestParam @Min(value = 1, message = "cantidad debe ser al menos 1") Integer cantidad) {
        return servicio.actualizar(id, cantidad);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
