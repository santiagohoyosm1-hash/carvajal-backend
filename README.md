# Carvajal Wishlist – ms-wishlist

Microservicio de lista de deseos. Java 21, Spring Boot 3.5.7, Spring Data JPA y **MySQL 8**.

```
carvajal-wishlist-microservicios/
├── database/            scripts SQL para MySQL Workbench
├── docker-compose.yml   MySQL local
└── ms-wishlist/         pom.xml, Dockerfile y código (puerto 8082)
```

## Ejecutar en local
1. `docker compose up -d` (MySQL en `localhost:3306`, base `wishlist_db`, usuario `carvajal` / `carvajal123`).
2. `cd ms-wishlist && mvn spring-boot:run`
3. Pruebas: `mvn test`

## Endpoints (`/api/wishlist`)
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/{usuarioId}` | Lista los deseos del usuario | 200 |
| POST | `/` | `{ "usuarioId":1, "productoId":1, "cantidadDeseada":2 }` (si ya existe, actualiza la cantidad) | 201, 400, 404, 409, 503 |
| PATCH | `/{id}?cantidad=3` | Cambia la cantidad | 200, 400, 404, 409, 503 |
| DELETE | `/{id}` | Elimina el deseo | 204, 404 |

Errores con formato único: `{ "status":404, "error":"Not Found", "mensaje":"...", "fecha":"..." }`.

## Patrones (según la Guía de Patrones de Diseño)
- **Adapter** (paquete `catalogo`): `DeseoService` depende de la interfaz `CatalogoProductos`; `ProductosHttpAdapter` traduce la API de `ms-productos` a `ProductoCatalogo`. Validan: el producto existe, está activo y hay stock. Al bajar la cantidad no se consulta el catálogo.
- **Singleton**: `ProductosProperties` (`@ConfigurationProperties`) y todos los beans.
- **Builder**: `DeseoResponse` con `@Builder` de Lombok y `ResponseEntity.created(...)`.
- **Capas**: controlador delgado → servicio → repositorio; DTOs en `dto`; manejo global de errores en `exception`.

## MySQL Workbench
1. Nueva conexión: host `localhost`, puerto `3306`, usuario `carvajal`, clave `carvajal123`.
2. Ejecuta `database/01_wishlist_db.sql` (opcional: Hibernate con `ddl-auto=update` crea la tabla).
3. Para la revisión usa `database/02_consultas_revision.sql`.

Para conectarte a la base de **Railway** desde Workbench: servicio MySQL → pestaña *Connect* → *Public Network* (host y puerto del proxy TCP) y el usuario/clave de *Variables* (`MYSQLUSER`, `MYSQLPASSWORD`).

## Despliegue en Railway
1. Crea el servicio **MySQL** en el proyecto.
2. Crea el servicio de `ms-wishlist` desde GitHub: *Settings → Root Directory* = `ms-wishlist` (usa el `Dockerfile`).
3. Variables de `ms-wishlist` (referencias al servicio MySQL, ajusta el nombre `MySQL` si es otro):

```
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
PRODUCTOS_URL=http://ms-productos.railway.internal:<PORT de ms-productos>
CORS_ORIGINS=https://<tu-frontend>.up.railway.app
```
`PORT` lo inyecta Railway; no lo definas. El puerto interno de `ms-productos` es el `PORT` de ese servicio (fíjalo en 8081 con la variable `PORT=8081` si quieres mantener la URL del README anterior).

## Git Flow
```bash
git init -b main
git add . && git commit -m "chore: base ms-wishlist"
git remote add origin <URL-repo-backend> && git push -u origin main
git flow init -d && git push -u origin develop

git flow feature start revision-final        # trabajo nuevo
git flow feature finish revision-final
git flow release start 1.0.0
git flow release finish 1.0.0                # fusiona en main y develop, crea el tag
git push origin main develop --tags
```

## Pendiente (fuera de este microservicio)
RabbitMQ (eventos), seguridad, y los demás microservicios (`api-gateway`, `ms-productos`, `ms-historial`, `ms-notificaciones`), que no venían en el zip. Cámbialos también a MySQL (`mysql-connector-j` y `jdbc:mysql://`).
