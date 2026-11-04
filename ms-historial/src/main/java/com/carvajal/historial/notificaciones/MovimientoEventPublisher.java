package com.carvajal.historial.notificaciones;

import com.carvajal.contracts.MovimientoRegistradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MovimientoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MovimientoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public MovimientoEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.exchange}") String exchange,
            @Value("${app.messaging.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publicar(MovimientoRegistradoEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event, message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            });
        } catch (AmqpException e) {
            log.warn("No se pudo publicar el evento de movimiento: {}", e.getMessage());
        }
    }
}
