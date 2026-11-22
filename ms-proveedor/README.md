# ms-proveedor · Portal del vendedor (issue #41)

Microservicio Spring Boot (Kotlin, puerto **8082**) que respalda en el backend la issue
**#41 · Refactor ProveedorViewModel y Cámara**:

| Check de la issue | Qué hace este microservicio |
|---|---|
| RF-05: el formulario envía el objeto con la **foto nativa** vía Retrofit | `POST /api/proveedor/lotes` recibe el mismo JSON que envía `ProveedorViewModel` (con `fotoBase64`), **valida que la foto sea un JPEG/PNG real** (≤ 2 MB), pide a **MS-Core** que publique el lote (RF-04 y RF-06) y guarda la foto y la publicación en su propia base |
| RF-07: la vista de métricas consume los **datos de ventas del backend** | `GET /api/proveedor/{id}/metricas` obtiene las ventas desde MS-Core y les suma las publicaciones hechas desde la app y cuántas tienen foto |

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/proveedor/lotes` | Publicar un lote con foto → 201 |
| GET | `/api/proveedor/fotos/{id}` | Ver la foto guardada (image/jpeg o image/png) |
| GET | `/api/proveedor/{idProveedor}/publicaciones` | Historial de publicaciones |
| GET | `/api/proveedor/{idProveedor}/metricas` | Métricas de ventas del vendedor |

Errores en JSON: `{ "error": "..." }` → 400 (foto o datos inválidos, alimento fresco), 404, 503 (MS-Core apagado).

## Comunicación entre microservicios

```
App Android ──Retrofit──► ms-proveedor :8082 ──RestClient──► ms-core :8080
                              │  (fotos_lote, publicaciones)      (lotes, ventas)
```

## Ejecutar

```powershell
# Requiere ms-core corriendo en el puerto 8080
cd ms-proveedor
.\mvnw.cmd spring-boot:run      # http://localhost:8082
.\mvnw.cmd test                 # pruebas (validación de fotos)
```

Pruebas manuales: `pruebas-ms-proveedor.http` (extensión REST Client de VS Code; ya trae una foto JPEG de ejemplo).

## Conectar la app (opcional)

Las respuestas incluyen los mismos campos que esperan `LotePublicadoDto` y `MetricasVendedorDto` de la app.
Para que la app use este microservicio, en `RetrofitClient.kt` cambia `BASE_URL_CORE` a `http://10.0.2.2:8082/`
y en `CoreApiService.kt` usa las rutas `api/proveedor/lotes` y `api/proveedor/{idProveedor}/metricas`.
