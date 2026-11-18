package com.carvajal.historial.dto;

import com.carvajal.historial.entity.Movimiento;
import java.time.LocalDateTime;

public record MovimientoResponse(Long id, Long usuarioId, Long productoId, String accion,
                                 Integer cantidad, LocalDateTime fecha, String detalle) {

    public static MovimientoResponse de(Movimiento m) {
        return new MovimientoResponse(m.getId(), m.getUsuarioId(), m.getProductoId(), m.getAccion(),
                m.getCantidad(), m.getFecha(), m.getDetalle());
    }
}
