package com.carvajal.wishlist.catalogo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración leída de application.yml (clave "productos.url"); un solo bean compartido (Singleton). */
@ConfigurationProperties(prefix = "productos")
public record ProductosProperties(String url) { }
