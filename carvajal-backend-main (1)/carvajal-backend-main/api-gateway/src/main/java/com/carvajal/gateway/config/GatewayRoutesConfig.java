package com.carvajal.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouteLocator apiRoutes(
            RouteLocatorBuilder builder,
            @Value("${gateway.services.wishlist-url}") String wishlistUrl,
            @Value("${gateway.services.productos-url}") String productosUrl,
            @Value("${gateway.services.historial-url}") String historialUrl,
            @Value("${gateway.services.notificaciones-url}") String notificacionesUrl) {
        return builder.routes()
                .route("wishlist-auth", route -> route.path("/api/auth/**").uri(wishlistUrl))
                .route("wishlist", route -> route.path("/api/wishlist/**").uri(wishlistUrl))
                .route("productos", route -> route.path("/api/productos/**").uri(productosUrl))
                .route("historial", route -> route.path("/api/historial/**").uri(historialUrl))
                .route("notificaciones", route -> route.path("/api/notificaciones/**").uri(notificacionesUrl))
                .build();
    }
}
