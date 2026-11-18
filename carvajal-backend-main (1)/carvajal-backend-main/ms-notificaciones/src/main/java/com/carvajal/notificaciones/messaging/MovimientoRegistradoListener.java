package com.carvajal.notificaciones.messaging;

import com.carvajal.contracts.MovimientoRegistradoEvent;
import com.carvajal.notificaciones.dto.NotificacionRequest;
import com.carvajal.notificaciones.service.NotificacionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MovimientoRegistradoListener {

    private final NotificacionService service;

    public MovimientoRegistradoListener(NotificacionService service) {
        this.service = service;
    }

    @RabbitListener(queues = "${app.messaging.queue}")
    public void recibir(MovimientoRegistradoEvent event) {
        service.crear(new NotificacionRequest(event.usuarioId(), event.productoId(), event.mensaje()));
    }
}
