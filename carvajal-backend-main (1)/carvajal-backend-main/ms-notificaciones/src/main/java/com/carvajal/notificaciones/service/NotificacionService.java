package com.carvajal.notificaciones.service;

import com.carvajal.notificaciones.dto.NotificacionRequest;
import com.carvajal.notificaciones.dto.NotificacionResponse;
import com.carvajal.notificaciones.entity.Notificacion;
import com.carvajal.notificaciones.exception.NoEncontradoException;
import com.carvajal.notificaciones.repository.NotificacionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    private final NotificacionRepository repo;

    public NotificacionService(NotificacionRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar(Long usuarioId) {
        return repo.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream().map(NotificacionResponse::de).toList();
    }

    @Transactional
    public NotificacionResponse crear(NotificacionRequest r) {
        Notificacion n = new Notificacion();
        n.setUsuarioId(r.usuarioId());
        n.setProductoId(r.productoId());
        n.setMensaje(r.mensaje());
        return NotificacionResponse.de(repo.save(n));
    }

    @Transactional
    public NotificacionResponse marcarLeida(Long id) {
        Notificacion n = repo.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la notificacion " + id));
        n.setLeida(true);
        return NotificacionResponse.de(repo.save(n));
    }
}
