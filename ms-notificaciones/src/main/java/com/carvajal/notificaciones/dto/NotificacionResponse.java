package com.carvajal.notificaciones.dto;

import com.carvajal.notificaciones.entity.Notificacion;
import java.time.LocalDateTime;

public record NotificacionResponse(Long id, Long usuarioId, Long productoId, String mensaje,
                                   boolean leida, LocalDateTime fecha) {

    public static NotificacionResponse de(Notificacion n) {
        return new NotificacionResponse(n.getId(), n.getUsuarioId(), n.getProductoId(), n.getMensaje(),
                n.isLeida(), n.getFecha());
    }
}
