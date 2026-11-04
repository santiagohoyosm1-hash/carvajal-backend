package com.carvajal.historial.notificaciones;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Avisa a ms-notificaciones cuando se registra un movimiento (mejor esfuerzo).
 * Si NOTIFICACIONES_URL no esta definida, o el servicio no responde, el historial se guarda igual.
 */
@Component
public class NotificadorHttp {

    private static final Logger log = LoggerFactory.getLogger(NotificadorHttp.class);

    private final RestClient client;

    public NotificadorHttp(@Value("${notificaciones.url:}") String url) {
        if (url == null || url.isBlank()) {
            this.client = null;
        } else {
            JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
            factory.setReadTimeout(Duration.ofSeconds(3));
            this.client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        }
    }

    public void notificar(Long usuarioId, Long productoId, String mensaje) {
        if (client == null) {
            return;
        }
        try {
            client.post().uri("/api/notificaciones")
                    .body(Map.of("usuarioId", usuarioId, "productoId", productoId, "mensaje", mensaje))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo crear la notificacion: {}", e.getMessage());
        }
    }
}
