# Issue #41 · Refactor ProveedorViewModel y cámara

Archivos de la app Android CostoStock (Kotlin + Jetpack Compose) para esta issue. Cada archivo conserva su ruta original: se copia el contenido de `app/` sobre la carpeta `app/` del proyecto Android, reemplazando los existentes.

## Archivos

| Archivo | Estado | Qué hace |
|---|---|---|
| `network/RetrofitClient.kt` | modificado | Agrega `coreApi`, el cliente Retrofit hacia MS-Core (`http://10.0.2.2:8080/`) |
| `network/CoreApiService.kt` | nuevo | `POST api/lotes` y `GET api/proveedores/{id}/metricas` |
| `network/CoreDtos.kt` | nuevo | `PublicarLoteRequest`, `LotePublicadoDto` y `MetricasVendedorDto` |
| `util/ImagenUtils.kt` | nuevo | `Bitmap.aBase64()`: convierte la foto nativa en texto para el JSON |
| `viewmodel/ProveedorViewModel.kt` | modificado | RF-05: envía el lote con la foto vía Retrofit. RF-07: carga las métricas desde el backend |
| `ui/PublicarLoteScreen.kt` | modificado | Botón con estado "Enviando…" y error del servidor |
| `ui/ValidarVoucherScreen.kt` | modificado | Tarjeta de métricas de ventas desde MS-Core + "Actualizar" |

## Tests

`app/src/test/.../Issue41ProveedorViewModelTest.kt`: métricas desde MS-Core (RF-07), respaldo sin conexión y comisión del 10 % (RF-06).

## Probar

1. Iniciar `ms-core` (puerto 8080).
2. Ejecutar la app en el emulador: **Ofertas → Publicar lote** (con foto) y **Validar voucher** (métricas).
3. `.\gradlew.bat testDebugUnitTest`
