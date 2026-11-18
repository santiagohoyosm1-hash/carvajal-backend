package com.carvajal.notificaciones.repository;

import com.carvajal.notificaciones.entity.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);
}
