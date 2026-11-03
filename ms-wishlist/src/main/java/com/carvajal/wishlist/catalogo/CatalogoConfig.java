package com.carvajal.wishlist.catalogo;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ProductosProperties.class)
public class CatalogoConfig {

    /** Cliente HTTP hacia ms-productos, con tiempos máximos para que wishlist no se quede colgado. */
    @Bean
    RestClient productosRestClient(RestClient.Builder builder, ProductosProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3));
        return builder.baseUrl(props.url()).requestFactory(factory).build();
    }
}
