package com.carvajal.historial.repository;

import com.carvajal.historial.entity.Movimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);
}
