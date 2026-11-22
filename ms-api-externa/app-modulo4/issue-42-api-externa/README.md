# Issue #42 · Integración de API externa pública

Archivos de la app Android CostoStock para esta issue. Se usa **Open-Meteo** (clima, pública y sin API key) con una instancia de Retrofit **separada** de la de los microservicios. Cada archivo conserva su ruta original: se copia el contenido de `app/` sobre la carpeta `app/` del proyecto Android.

## Archivos

| Archivo | Estado | Qué hace |
|---|---|---|
| `network/externa/ClimaRetrofitClient.kt` | nuevo | Instancia de Retrofit **separada**, hacia `https://api.open-meteo.com/` |
| `network/externa/ClimaApiService.kt` | nuevo | `GET v1/forecast` (clima actual, sin API key) |
| `network/externa/ClimaDto.kt` | nuevo | Mapeo de la respuesta JSON con `@SerializedName` |
| `viewmodel/ClimaViewModel.kt` | nuevo | Estado Cargando / Éxito / Error y selección de ciudad |
| `ui/ClimaScreen.kt` | nuevo | Vista `@Composable` dedicada, sin acoplarse a Spring Boot |
| `ui/AppNavigation.kt` | modificado | Agrega la pestaña "Clima" |

## Tests

`app/src/test/.../Issue42ClimaViewModelTest.kt`: clima de la ciudad seleccionada y error sin conexión.

## Probar

1. Ejecutar la app en el emulador (con internet) y abrir la pestaña **🌤️ Clima**.
2. `.\gradlew.bat testDebugUnitTest`

No se agregan dependencias: Retrofit y Gson ya existen en la app.
