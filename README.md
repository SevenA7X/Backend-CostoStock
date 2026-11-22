# CostoStock · Backend + App (Evaluación parcial 3)

Repositorio con los microservicios Spring Boot y la app Android que los consume.

| Carpeta | Qué es | Puerto |
|---|---|---|
| `ms-transacciones/` | Catálogo de ofertas (`GET /api/catalogo`) | 8081 |
| `ms-core/` | Gestión de ofertas: CRUD de lotes, comisión 10 %, métricas del vendedor | 8080 |
| `app-costostock/` | App Android (Kotlin + Jetpack Compose + Retrofit) | — |

## Módulo 4

### #40 · Backend MS-Core (Spring Boot)
- `POST / GET / PUT / DELETE /api/lotes`: publicación de lotes. Se rechazan los alimentos frescos (RF-04) con `400 {"error": "..."}`.
- El controlador (`LoteController.calcularPrecioFinal`) calcula el 10 % de comisión y lo suma al precio final (RF-06).
- `GET /api/lotes/simular-precio?montoLiquido=5000` muestra el desglose del precio.
- `PATCH /api/lotes/{id}/ventas` registra ventas y `GET /api/proveedores/{id}/metricas` entrega las métricas del vendedor.

### #41 · Refactor ProveedorViewModel y cámara (app)
- `ProveedorViewModel` envía el lote, con la foto nativa en Base64, a MS-Core vía Retrofit (RF-05).
- La tarjeta de métricas de `ValidarVoucherScreen` consume las ventas desde MS-Core (RF-07).

### #42 · API externa pública (app)
- `network/externa/ClimaRetrofitClient`: instancia de Retrofit **separada** que consume Open-Meteo (clima, sin API key).
- `ClimaScreen` (`@Composable`) + `ClimaViewModel`: pestaña "Clima", no acoplada a los microservicios.

## Cómo ejecutar

```powershell
# Terminal 1
cd ms-core
.\mvnw.cmd spring-boot:run        # http://localhost:8080

# Terminal 2
cd ms-transacciones
.\mvnw.cmd spring-boot:run        # http://localhost:8081
```

- Pruebas de los endpoints: `ms-core/pruebas-ms-core.http` (extensión **REST Client** de VS Code).
- Tests del backend: `cd ms-core` y luego `.\mvnw.cmd test`.
- App: abrir la carpeta `app-costostock/` en **Android Studio** y ejecutar en el emulador. Desde el emulador, el PC se alcanza en `10.0.2.2`.
- Tests de la app: `cd app-costostock` y luego `.\gradlew.bat testDebugUnitTest`.
