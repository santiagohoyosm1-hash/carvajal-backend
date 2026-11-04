package com.carvajal.productos.service;

import com.carvajal.productos.dto.ProductoRequest;
import com.carvajal.productos.dto.ProductoResponse;
import com.carvajal.productos.entity.Producto;
import com.carvajal.productos.exception.NoEncontradoException;
import com.carvajal.productos.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    /** Solo productos ACTIVOS: ms-wishlist depende de este comportamiento (ver ProductosHttpAdapter). */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarActivos() {
        return repo.findByActivoTrueOrderByIdAsc().stream().map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscar(Long id) {
        return ProductoResponse.de(obtener(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest r) {
        Producto p = new Producto();
        aplicar(p, r);
        return ProductoResponse.de(repo.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest r) {
        Producto p = obtener(id);
        aplicar(p, r);
        return ProductoResponse.de(repo.save(p));
    }

    /** Borrado logico: el producto queda inactivo y deja de aparecer en el catalogo. */
    @Transactional
    public void eliminar(Long id) {
        Producto p = obtener(id);
        p.setActivo(false);
        repo.save(p);
    }

    private Producto obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el producto " + id));
    }

    private void aplicar(Producto p, ProductoRequest r) {
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion());
        p.setPrecio(r.precio());
        p.setStock(r.stock());
        p.setCategoria(r.categoria());
        p.setActivo(r.activo() == null || r.activo());
    }
}
