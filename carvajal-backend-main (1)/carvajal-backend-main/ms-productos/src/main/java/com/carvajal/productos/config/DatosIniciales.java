package com.carvajal.productos.config;

import com.carvajal.productos.entity.Producto;
import com.carvajal.productos.repository.ProductoRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Carga unos productos de ejemplo solo si la tabla esta vacia. Desactivar con SEED_PRODUCTOS=false. */
@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarProductos(ProductoRepository repo,
                                      @org.springframework.beans.factory.annotation.Value("${seed.productos:true}") boolean seed) {
        return args -> {
            if (!seed || repo.count() > 0) {
                return;
            }
            crear(repo, "Teclado mecanico", "Switches rojos, layout espanol", "189900", 25, "Perifericos");
            crear(repo, "Mouse inalambrico", "Sensor optico 1600 dpi", "69900", 40, "Perifericos");
            crear(repo, "Monitor 24 pulgadas", "Full HD, panel IPS", "649900", 12, "Pantallas");
            crear(repo, "Auriculares con microfono", "Cancelacion de ruido", "259900", 18, "Audio");
            crear(repo, "Webcam HD", "1080p con microfono integrado", "119900", 30, "Video");
        };
    }

    private static void crear(ProductoRepository repo, String nombre, String desc, String precio, int stock, String cat) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(desc);
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        p.setCategoria(cat);
        repo.save(p);
    }
}
