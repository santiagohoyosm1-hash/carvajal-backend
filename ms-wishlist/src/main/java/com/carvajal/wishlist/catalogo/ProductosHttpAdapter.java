package com.carvajal.wishlist.catalogo;

import com.carvajal.wishlist.exception.CatalogoNoDisponibleException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * ADAPTADOR: traduce la API HTTP de ms-productos a la interfaz CatalogoProductos.
 *
 * Se consulta GET /api/productos (que solo devuelve productos ACTIVOS) y se busca el id ahí.
 * Así "no existe" e "inactivo" se resuelven igual (vacío) sin depender de cómo responda
 * ms-productos ante un id inexistente (hoy lanza una RuntimeException => 500).
 * Si ms-productos agrega un 404 limpio, basta cambiar este método a GET /api/productos/{id};
 * nada fuera de esta clase se entera.
 */
@Component
public class ProductosHttpAdapter implements CatalogoProductos {

    private final RestClient restClient;

    public ProductosHttpAdapter(RestClient productosRestClient) {
        this.restClient = productosRestClient;
    }

    @Override
    public Optional<ProductoCatalogo> buscarPorId(Long productoId) {
        try {
            List<ProductoExterno> productos = restClient.get()
                    .uri("/api/productos")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProductoExterno>>() { });
            if (productos == null) {
                return Optional.empty();
            }
            return productos.stream()
                    .filter(p -> Objects.equals(p.id(), productoId))
                    .findFirst()
                    .map(this::aModeloPropio);
        } catch (RestClientException e) {
            throw new CatalogoNoDisponibleException(e);
        }
    }

    /** Traducción del formato de ms-productos al modelo de wishlist. */
    private ProductoCatalogo aModeloPropio(ProductoExterno p) {
        return new ProductoCatalogo(p.id(), p.nombre(), p.precio(), p.stock() == null ? 0 : p.stock(), p.activo());
    }

    /** Formato de ms-productos (solo los campos que usamos; el resto se ignora). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProductoExterno(Long id, String nombre, BigDecimal precio, Integer stock, boolean activo) { }
}
