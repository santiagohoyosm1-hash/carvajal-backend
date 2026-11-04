package com.carvajal.historial.service;

import com.carvajal.historial.dto.MovimientoRequest;
import com.carvajal.historial.dto.MovimientoResponse;
import com.carvajal.historial.entity.Movimiento;
import com.carvajal.contracts.MovimientoRegistradoEvent;
import com.carvajal.historial.notificaciones.MovimientoEventPublisher;
import com.carvajal.historial.repository.MovimientoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialService {

    private final MovimientoRepository repo;
    private final MovimientoEventPublisher eventPublisher;

    public HistorialService(MovimientoRepository repo, MovimientoEventPublisher eventPublisher) {
        this.repo = repo;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listar(Long usuarioId) {
        return repo.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream().map(MovimientoResponse::de).toList();
    }

    public MovimientoResponse registrar(MovimientoRequest r) {
        Movimiento m = new Movimiento();
        m.setUsuarioId(r.usuarioId());
        m.setProductoId(r.productoId());
        m.setAccion(r.accion().trim().toUpperCase());
        m.setCantidad(r.cantidad());
        m.setDetalle(r.detalle());
        MovimientoResponse guardado = MovimientoResponse.de(repo.save(m));
        String texto = r.detalle() != null && !r.detalle().isBlank() ? r.detalle() : "Movimiento " + guardado.accion();
        eventPublisher.publicar(new MovimientoRegistradoEvent(r.usuarioId(), r.productoId(), texto));
        return guardado;
    }
}
