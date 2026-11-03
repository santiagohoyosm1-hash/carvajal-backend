package com.carvajal.wishlist.service;

import com.carvajal.wishlist.catalogo.CatalogoProductos;
import com.carvajal.wishlist.catalogo.ProductoCatalogo;
import com.carvajal.wishlist.dto.DeseoRequest;
import com.carvajal.wishlist.dto.DeseoResponse;
import com.carvajal.wishlist.entity.Deseo;
import com.carvajal.wishlist.exception.DeseoNoEncontradoException;
import com.carvajal.wishlist.exception.ProductoNoDisponibleException;
import com.carvajal.wishlist.exception.StockInsuficienteException;
import com.carvajal.wishlist.repository.DeseoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DeseoService {

    private final DeseoRepository repo;
    private final CatalogoProductos catalogo; // depende de la interfaz, no del adaptador HTTP

    public DeseoService(DeseoRepository repo, CatalogoProductos catalogo) {
        this.repo = repo;
        this.catalogo = catalogo;
    }

    public List<DeseoResponse> listar(Long usuarioId) {
        return repo.findByUsuarioId(usuarioId).stream().map(this::aResponse).toList();
    }

    // Sin @Transactional a propósito: no se debe mantener una transacción abierta mientras se llama por HTTP.
    public DeseoResponse agregar(DeseoRequest req) {
        int cantidad = req.cantidadDeseada() == null ? 1 : req.cantidadDeseada();
        validarDisponibilidad(req.productoId(), cantidad);

        Deseo guardado = repo.findByUsuarioIdAndProductoId(req.usuarioId(), req.productoId())
                .map(existente -> {
                    existente.setCantidadDeseada(cantidad);
                    return repo.save(existente);
                })
                .orElseGet(() -> {
                    Deseo nuevo = new Deseo();
                    nuevo.setUsuarioId(req.usuarioId());
                    nuevo.setProductoId(req.productoId());
                    nuevo.setCantidadDeseada(cantidad);
                    return repo.save(nuevo);
                });
        return aResponse(guardado);
    }

    public DeseoResponse actualizar(Long id, Integer cantidad) {
        Deseo deseo = repo.findById(id).orElseThrow(() -> new DeseoNoEncontradoException(id));
        // Bajar la cantidad siempre se permite; solo se consulta el catálogo si se pide más.
        if (deseo.getCantidadDeseada() == null || cantidad > deseo.getCantidadDeseada()) {
            validarDisponibilidad(deseo.getProductoId(), cantidad);
        }
        deseo.setCantidadDeseada(cantidad);
        return aResponse(repo.save(deseo));
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new DeseoNoEncontradoException(id);
        }
        repo.deleteById(id);
    }

    private void validarDisponibilidad(Long productoId, int cantidad) {
        ProductoCatalogo producto = catalogo.buscarPorId(productoId)
                .filter(ProductoCatalogo::activo)
                .orElseThrow(() -> new ProductoNoDisponibleException(productoId));
        if (cantidad > producto.stock()) {
            throw new StockInsuficienteException(producto.nombre(), producto.stock(), cantidad);
        }
    }

    private DeseoResponse aResponse(Deseo d) {
        return DeseoResponse.builder()
                .id(d.getId())
                .usuarioId(d.getUsuarioId())
                .productoId(d.getProductoId())
                .cantidadDeseada(d.getCantidadDeseada())
                .fechaCreacion(d.getFechaCreacion())
                .fechaActualizacion(d.getFechaActualizacion())
                .build();
    }
}
