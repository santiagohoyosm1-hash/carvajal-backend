package com.carvajal.wishlist.repository;

import com.carvajal.wishlist.entity.Deseo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeseoRepository extends JpaRepository<Deseo, Long> {

    List<Deseo> findByUsuarioId(Long usuarioId);

    Optional<Deseo> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
}
