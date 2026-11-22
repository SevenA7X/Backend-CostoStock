# ms-clima · Clima y ecología (issue #42)

Microservicio Spring Boot (Kotlin, puerto **8083**) que complementa la issue
**#42 · Integración de API Externa Pública**. Consume la API REST pública **Open-Meteo**
(https://open-meteo.com, gratuita y sin API key) y entrega el clima listo para mostrar,
con un consejo ecológico de CostoStock.

| Check de la issue | Relación con este microservicio |
|---|---|
| Instancia separada para consumir una API REST ajena (Guía 14) | `OpenMeteoClient` es un cliente HTTP propio y separado, solo para Open-Meteo |
| Vista `@Composable` dedicada, **sin acoplarla a Spring Boot** | La pantalla `ClimaScreen` de la app **sigue llamando directo a Open-Meteo** (eso pide la issue). `ms-clima` es el apoyo en el backend: guarda historial, usa caché y sirve a otros clientes (por ejemplo, un panel web) |

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/clima?ciudad=VALPARAISO` | Clima actual (VALPARAISO, VINA_DEL_MAR, SANTIAGO) |
| GET | `/api/clima/coordenadas?lat=..&lon=..` | Clima de cualquier punto |
| GET | `/api/clima/ciudades` | Ciudades disponibles |
| GET | `/api/clima/historial?ciudad=..` | Últimas 20 consultas guardadas |

- **Caché:** si la misma ciudad se consultó hace menos de 10 minutos, responde con lo guardado (`"desdeCache": true`).
- **Respaldo:** si Open-Meteo no responde, devuelve la última medición guardada con un `aviso`; si no hay ninguna, responde 503.

## Ejecutar

```powershell
cd ms-clima
.\mvnw.cmd spring-boot:run      # http://localhost:8083  (necesita internet)
.\mvnw.cmd test                 # pruebas (traducción de códigos y consejos)
```

Pruebas manuales: `pruebas-ms-clima.http` (extensión REST Client de VS Code).
Datos: Open-Meteo.com, licencia CC BY 4.0.
