# Carvajal Wishlist – ms-wishlist

Microservicio de lista de deseos. Java 21, Spring Boot 3.5.7, Spring Data JPA y **MySQL 8**.

```
carvajal-wishlist-microservicios/
├── database/            scripts SQL para MySQL Workbench
├── docker-compose.yml   MySQL y RabbitMQ locales
├── api-gateway/         entrada HTTP, JWT y enrutamiento (puerto 8080)
└── ms-wishlist/         pom.xml, Dockerfile y código (puerto 8082)
```

## Ejecutar en local
1. `docker compose up -d` (MySQL en `localhost:3306` y RabbitMQ en `localhost:5672`; consola de RabbitMQ en `http://localhost:15672`, usuario `carvajal` / `carvajal123`).
2. Configura `JWT_SECRET`, `AUTH_ADMIN_USERNAME` y `AUTH_ADMIN_PASSWORD` para los procesos de Spring Boot, como se explica en la sección JWT.
3. Inicia los cuatro microservicios en terminales separadas desde sus carpetas (`ms-productos`, `ms-wishlist`, `ms-historial` y `ms-notificaciones`) con `mvn spring-boot:run`.
4. Inicia el gateway con `mvn spring-boot:run` desde `api-gateway`. El frontend y los clientes usarán `http://localhost:8080`.
5. Ejecuta `mvn test` desde cada servicio para probarlo.

Para ejecutar los servicios que usan RabbitMQ, abre otra terminal para cada uno y ejecuta `mvn spring-boot:run` desde `ms-historial` y `ms-notificaciones`. Ambos se conectan por defecto al RabbitMQ local del `docker compose`.

## Endpoints (`/api/wishlist`)
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/{usuarioId}` | Lista los deseos del usuario | 200 |
| POST | `/` | `{ "usuarioId":1, "productoId":1, "cantidadDeseada":2 }` (si ya existe, actualiza la cantidad) | 201, 400, 404, 409, 503 |
| PATCH | `/{id}?cantidad=3` | Cambia la cantidad | 200, 400, 404, 409, 503 |
| DELETE | `/{id}` | Elimina el deseo | 204, 404 |

Errores con formato único: `{ "status":404, "error":"Not Found", "mensaje":"...", "fecha":"..." }`.

## Autenticación JWT

Los cuatro microservicios requieren un JWT Bearer válido en sus rutas `/api/**`, excepto el login. `ms-wishlist` emite tokens HS256 de 15 minutos; usa la tabla `auth_users` y asigna el `usuarioId` automáticamente. No hay registro público: el administrador inicial se crea con `AUTH_ADMIN_USERNAME` y `AUTH_ADMIN_PASSWORD`, y solo un token con rol `ADMIN` puede crear cuentas en `POST /api/auth/users`. La contraseña de las cuentas debe tener al menos 12 caracteres.

Genera una clave compartida segura de 32 bytes en Base64 (por ejemplo, con `openssl rand -base64 32`) y configura la misma `JWT_SECRET` en los cuatro microservicios. En PowerShell, puedes crearla y guardar las variables para tu usuario así:

```powershell
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
[Environment]::SetEnvironmentVariable("JWT_SECRET", [Convert]::ToBase64String($bytes), "User")
[Environment]::SetEnvironmentVariable("AUTH_ADMIN_USERNAME", "admin", "User")
[Environment]::SetEnvironmentVariable("AUTH_ADMIN_PASSWORD", "<contraseña-segura-de-al-menos-12-caracteres>", "User")
$rng.Dispose()
```

Abre terminales nuevas después de configurarlas. `AUTH_ADMIN_USERNAME` y `AUTH_ADMIN_PASSWORD` solo se usan en `ms-wishlist`. No guardes la clave ni las credenciales en el repositorio.

1. Login: `POST /api/auth/login` con `{"username":"<usuario>","password":"<contraseña>"}`. La respuesta contiene `accessToken`, `tokenType`, `expiresIn`, `usuarioId` y `role`.
2. Crear una cuenta: `POST /api/auth/users` con `{"username":"<usuario>","password":"<contraseña>"}` y el header `Authorization: Bearer <token-admin>`.
3. Envía `Authorization: Bearer <accessToken>` en las llamadas a los cuatro servicios. `ms-wishlist` reenvía el token al consultar `ms-productos`.

Los tokens autentican la llamada, pero por ahora no restringen los `usuarioId` de las rutas o del cuerpo a la identidad del token. Los cuatro servicios deben compartir exactamente la misma clave; cambiarla invalida los tokens emitidos anteriormente.

## API Gateway

`api-gateway` usa Spring Cloud Gateway WebFlux y escucha en el puerto `8080`. Mantiene los prefijos de ruta y enruta `/api/auth/**` y `/api/wishlist/**` a `ms-wishlist`, `/api/productos/**` a `ms-productos`, `/api/historial/**` a `ms-historial` y `/api/notificaciones/**` a `ms-notificaciones`. El gateway valida los JWT antes de reenviarlos y solo deja público `POST /api/auth/login`; `POST /api/auth/users` requiere rol `ADMIN`. Los microservicios conservan su propia validación JWT.

En local, las URL por defecto apuntan a `localhost` y a los puertos de la tabla. Para desplegarlo, configura `WISHLIST_URL`, `PRODUCTOS_URL`, `HISTORIAL_URL` y `NOTIFICACIONES_URL` con las direcciones privadas de los servicios. Usa el gateway como único destino público del frontend y evita exponer los microservicios directamente.

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
3. Configura las variables de `ms-wishlist`:

```
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
PRODUCTOS_URL=http://ms-productos.railway.internal:<PORT de ms-productos>
CORS_ORIGINS=https://<tu-frontend>.up.railway.app
JWT_SECRET=<misma-clave-aleatoria-base64-en-los-cuatro-servicios>
AUTH_ADMIN_USERNAME=<usuario-admin-inicial>
AUTH_ADMIN_PASSWORD=<contraseña-admin-segura>
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

## Microservicios incluidos
| Servicio | Carpeta | Puerto local | Rutas |
|---|---|---|---|
| API Gateway | `api-gateway` | 8080 | Enruta las API de los cuatro servicios |
| Lista de deseos | `ms-wishlist` | 8082 | `/api/wishlist` |
| Catalogo | `ms-productos` | 8081 | `GET/POST /api/productos`, `GET/PUT/DELETE /api/productos/{id}` (DELETE = borrado logico; el GET solo devuelve activos; carga 5 productos de ejemplo si la tabla esta vacia) |
| Historial | `ms-historial` | 8083 | `GET /api/historial/{usuarioId}`, `POST /api/historial` |
| Notificaciones | `ms-notificaciones` | 8084 | `GET /api/notificaciones/{usuarioId}`, `POST /api/notificaciones`, `PATCH /api/notificaciones/{id}/leida` |

Los cuatro usan la misma base MySQL (`wishlist_db`) con tablas distintas (`deseos`, `productos`, `movimientos`, `notificaciones`), que Hibernate crea solo (`DDL_AUTO=update`).
Al registrar un movimiento, `ms-historial` publica por RabbitMQ el evento `historial.movimiento.registrado`; `ms-notificaciones` lo consume de la cola durable `notificaciones.movimiento.registrado` y persiste la notificación de forma asíncrona. Si RabbitMQ no está disponible al publicar, el movimiento se guarda igualmente y se registra una advertencia; los mensajes publicados son persistentes y la cola conserva los eventos mientras el consumidor está fuera de línea.

### Variables en Railway
Crea un servicio por carpeta (*New > GitHub Repo > este repo*) y configura *Settings > Root Directory* con la carpeta indicada. Publica el dominio de `api-gateway`; deja los microservicios en la red privada de Railway.

Variables para los cuatro microservicios:
```
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=<misma-clave-aleatoria-base64-en-los-cuatro-servicios>
```

Configura `PORT=8081` en `ms-productos`, `PORT=8082` en `ms-wishlist`, `PORT=8083` en `ms-historial` y `PORT=8084` en `ms-notificaciones`, para que el gateway pueda usar esos puertos privados. En `ms-wishlist`, configura `AUTH_ADMIN_USERNAME`, `AUTH_ADMIN_PASSWORD` y `PRODUCTOS_URL=http://ms-productos.railway.internal:8081`.

Variables para `api-gateway`:
```
JWT_SECRET=<la-misma-clave-aleatoria-base64>
CORS_ORIGINS=https://<tu-frontend>.vercel.app
WISHLIST_URL=http://ms-wishlist.railway.internal:8082
PRODUCTOS_URL=http://ms-productos.railway.internal:8081
HISTORIAL_URL=http://ms-historial.railway.internal:8083
NOTIFICACIONES_URL=http://ms-notificaciones.railway.internal:8084
```
Railway inyecta `PORT` al gateway. En `ms-historial` y `ms-notificaciones`, configura además `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER` y `RABBITMQ_PASSWORD` para el mismo broker; ambos declaran el exchange, la cola durable y su binding al iniciar.

### vercel.json del frontend
Cambia `URL-API-GATEWAY` por el dominio público del gateway:
```json
{
  "rewrites": [
    { "source": "/api/:path*",                "destination": "https://URL-API-GATEWAY/api/:path*" },
    { "source": "/api",                       "destination": "https://URL-API-GATEWAY/api" },
    { "source": "/((?!api/).*)",              "destination": "/index.html" }
  ]
}
```
## Pendiente
Vincular los `usuarioId` de las operaciones con la identidad del JWT. Mantén los microservicios en la red privada para que el gateway sea la única entrada desde Internet.
