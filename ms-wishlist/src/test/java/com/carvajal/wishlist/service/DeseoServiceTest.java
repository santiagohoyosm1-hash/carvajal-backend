package com.carvajal.wishlist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carvajal.wishlist.catalogo.CatalogoProductos;
import com.carvajal.wishlist.catalogo.ProductoCatalogo;
import com.carvajal.wishlist.dto.DeseoRequest;
import com.carvajal.wishlist.dto.DeseoResponse;
import com.carvajal.wishlist.entity.Deseo;
import com.carvajal.wishlist.exception.DeseoNoEncontradoException;
import com.carvajal.wishlist.exception.ProductoNoDisponibleException;
import com.carvajal.wishlist.exception.StockInsuficienteException;
import com.carvajal.wishlist.repository.DeseoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Gracias al Adapter, el catálogo se reemplaza por un falso de una línea: sin HTTP ni ms-productos. */
@ExtendWith(MockitoExtension.class)
class DeseoServiceTest {

    @Mock
    private DeseoRepository repo;

    private DeseoService servicioConCatalogo(Optional<ProductoCatalogo> producto) {
        CatalogoProductos catalogoFalso = id -> producto;
        return new DeseoService(repo, catalogoFalso);
    }

    private static DeseoRequest request(Long usuarioId, Long productoId, Integer cantidad) {
        return new DeseoRequest(usuarioId, productoId, cantidad);
    }

    private static Deseo deseo(Long usuarioId, Long productoId, int cantidad) {
        Deseo d = new Deseo();
        d.setUsuarioId(usuarioId);
        d.setProductoId(productoId);
        d.setCantidadDeseada(cantidad);
        return d;
    }

    private static ProductoCatalogo producto(int stock, boolean activo) {
        return new ProductoCatalogo(1L, "Cuaderno", new BigDecimal("12500"), stock, activo);
    }

    @Test
    void agregaCuandoElProductoTieneStock() {
        when(repo.findByUsuarioIdAndProductoId(1L, 1L)).thenReturn(Optional.empty());
        when(repo.save(any(Deseo.class))).thenAnswer(inv -> inv.getArgument(0));

        DeseoResponse resultado = servicioConCatalogo(Optional.of(producto(5, true))).agregar(request(1L, 1L, 2));

        assertEquals(2, resultado.getCantidadDeseada());
        verify(repo).save(any(Deseo.class));
    }

    @Test
    void sinCantidadSeAsumeUno() {
        when(repo.findByUsuarioIdAndProductoId(1L, 1L)).thenReturn(Optional.empty());
        when(repo.save(any(Deseo.class))).thenAnswer(inv -> inv.getArgument(0));

        DeseoResponse resultado = servicioConCatalogo(Optional.of(producto(5, true))).agregar(request(1L, 1L, null));

        assertEquals(1, resultado.getCantidadDeseada());
    }

    @Test
    void siYaExisteActualizaLaCantidadEnVezDeDuplicar() {
        Deseo existente = deseo(1L, 1L, 1);
        when(repo.findByUsuarioIdAndProductoId(1L, 1L)).thenReturn(Optional.of(existente));
        when(repo.save(existente)).thenReturn(existente);

        DeseoResponse resultado = servicioConCatalogo(Optional.of(producto(5, true))).agregar(request(1L, 1L, 3));

        assertEquals(3, resultado.getCantidadDeseada());
    }

    @Test
    void rechazaUnProductoQueNoExiste() {
        DeseoService servicio = servicioConCatalogo(Optional.empty());

        assertThrows(ProductoNoDisponibleException.class, () -> servicio.agregar(request(1L, 99L, 1)));
        verifyNoInteractions(repo);
    }

    @Test
    void rechazaUnProductoInactivo() {
        DeseoService servicio = servicioConCatalogo(Optional.of(producto(5, false)));

        assertThrows(ProductoNoDisponibleException.class, () -> servicio.agregar(request(1L, 1L, 1)));
        verifyNoInteractions(repo);
    }

    @Test
    void rechazaUnaCantidadMayorAlStock() {
        DeseoService servicio = servicioConCatalogo(Optional.of(producto(3, true)));

        assertThrows(StockInsuficienteException.class, () -> servicio.agregar(request(1L, 1L, 4)));
        verifyNoInteractions(repo);
    }

    @Test
    void permiteBajarLaCantidadSinConsultarElCatalogo() {
        Deseo existente = deseo(1L, 1L, 3);
        when(repo.findById(7L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Deseo.class))).thenAnswer(inv -> inv.getArgument(0));
        CatalogoProductos catalogoQueNoDebeUsarse = id -> {
            throw new AssertionError("No debía consultarse el catálogo al bajar la cantidad");
        };

        DeseoResponse resultado = new DeseoService(repo, catalogoQueNoDebeUsarse).actualizar(7L, 1);

        assertEquals(1, resultado.getCantidadDeseada());
    }

    @Test
    void subirLaCantidadConsultaElCatalogoYLaAcepta() {
        Deseo existente = deseo(1L, 1L, 1);
        when(repo.findById(7L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Deseo.class))).thenAnswer(inv -> inv.getArgument(0));

        DeseoResponse resultado = servicioConCatalogo(Optional.of(producto(5, true))).actualizar(7L, 4);

        assertEquals(4, resultado.getCantidadDeseada());
    }

    @Test
    void subirLaCantidadPorEncimaDelStockSeRechaza() {
        Deseo existente = deseo(1L, 1L, 1);
        when(repo.findById(7L)).thenReturn(Optional.of(existente));
        DeseoService servicio = servicioConCatalogo(Optional.of(producto(2, true)));

        assertThrows(StockInsuficienteException.class, () -> servicio.actualizar(7L, 3));
        verify(repo, never()).save(any(Deseo.class));
    }

    @Test
    void actualizarUnDeseoInexistenteLanzaNoEncontrado() {
        when(repo.findById(7L)).thenReturn(Optional.empty());
        DeseoService servicio = servicioConCatalogo(Optional.of(producto(5, true)));

        assertThrows(DeseoNoEncontradoException.class, () -> servicio.actualizar(7L, 2));
    }

    @Test
    void eliminarUnDeseoInexistenteLanzaNoEncontrado() {
        when(repo.existsById(7L)).thenReturn(false);
        DeseoService servicio = servicioConCatalogo(Optional.empty());

        assertThrows(DeseoNoEncontradoException.class, () -> servicio.eliminar(7L));
        verify(repo, never()).deleteById(any());
    }
}
