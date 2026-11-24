# 🛒 CostoStock · Backend (Microservicios Spring Boot)

> Backend del marketplace móvil **CostoStock**: venta de lotes de productos en sobrestock o próximos a vencer, con precio rebajado y cálculo del CO₂ evitado.

Este repositorio contiene los dos microservicios del sistema: **ms-core** y **ms-transacciones**.

---

## 1. Nombre del proyecto

**CostoStock — Backend de microservicios**
Asignatura: Desarrollo de Aplicaciones Móviles (DSY1105) · Evaluación Parcial 3

---

## 2. Integrantes

| Integrante | Correo | Aporte en el backend |
|---|---|---|
| Mario Antonio Quintero Almendras | ma.quinteroa@duocuc.cl | Estructura inicial del repositorio y MS-Transacciones (catálogo, cálculo de CO₂) |
| Romina Paz Carrasco Ruiz | ro.carrascor@duocuc.cl | Reservas con temporizador de 10 min, pagos simulados y registro de compradores |
| Nicolás Fernando Jaramillo Zamora | ni.jaramilloz@duocuc.cl | Vouchers y endpoints de historial (RF-14, RF-16, RF-17) |
| Nicol Andreina Valero López | nic.valero@duocuc.cl | MS-Core: publicación de lotes, comisión del 10 % y métricas del vendedor (Módulo 4) |

---

## 3. Funcionalidades

### Arquitectura

```
                 ┌──────────────────────────────┐
 Cliente HTTP ──►│ ms-core           :8080      │  Compradores, login de proveedores,
                 │ (H2 en archivo ./data/coredb)│  publicación de lotes y métricas
                 └──────────────────────────────┘
                 ┌──────────────────────────────┐
             ───►│ ms-transacciones  :8081      │  Catálogo con filtro GPS, reservas
                 │ (datos en memoria)           │  de 10 min y pagos simulados
                 └──────────────────────────────┘
```

| Microservicio | Puerto | Tecnología | Persistencia |
|---|---|---|---|
| `ms-core` | 8080 | Spring Boot 3.3.5 · Java 17 · Spring Data JPA | H2 en archivo (`ms-core/data/coredb`) |
| `ms-transacciones` | 8081 | Spring Boot 3.3.5 · Java 17 | En memoria (se reinicia al apagar) |

### ms-core (puerto 8080)
- **Registro ligero de compradores (RF-02):** nombre, correo y teléfono. Valida formato de correo y teléfono (solo dígitos, mínimo 8). Si el correo ya existe, actualiza los datos y conserva el mismo id.
- **Acceso de proveedores (RF-03):** login con correo y contraseña (guardada como hash SHA-256). Devuelve un token de sesión válido por 8 horas, que se usa en `Authorization: Bearer <token>`.
- **Publicación de lotes (RF-05 / RF-06):** guarda el lote con su foto en Base64 y calcula la **comisión de CostoStock del 10 %** sobre el monto líquido: `precio final = monto líquido + 10 %`.
- **Métricas del vendedor (RF-07):** lotes publicados y activos, unidades, ventas brutas, monto líquido acumulado, comisión generada, kg de CO₂ evitados y equivalencia en árboles (21 kg CO₂ = 1 árbol/año).
- **Health check** del servicio.

### ms-transacciones (puerto 8081)
- **Catálogo con filtro GPS (RF-08):** lista solo lotes activos, con stock y sin caducar. Si recibe `lat` y `lon`, filtra por radio (5, 15 o 30 km; 30 km por defecto) con la fórmula de **Haversine**.
- **Reserva temporal (RF-12):** al reservar se descuenta el stock y la reserva dura **10 minutos** (máximo 10 unidades). Calcula subtotal, **comisión de servicio del 5 %**, total y CO₂ evitado.
- **Liberación automática:** una tarea programada revisa cada 15 segundos y devuelve el stock de las reservas vencidas.
- **Cancelar reserva:** devuelve el stock (no se puede cancelar una reserva pagada).
- **Pago simulado (RF-13):** confirma la reserva y genera el **código del voucher QR** con los datos de retiro. Permite simular un pago rechazado.
- **Errores claros:** todas las respuestas de error tienen la forma `{"error": "...", "codigo": "..."}` con el código HTTP correspondiente (400, 401, 402, 404, 409, 410).

---

## 4. Endpoints utilizados

### ms-core — `http://localhost:8080`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/health` | Estado del servicio | 200 |
| POST | `/api/compradores/registro` | Registro ligero de comprador | 201 nuevo · 200 existente · 400 |
| GET | `/api/compradores/{id}` | Datos de un comprador | 200 · 404 |
| POST | `/api/proveedores/login` | Login del proveedor, devuelve token | 200 · 400 · 401 |
| GET | `/api/proveedores/acceso` | Valida el token (`Authorization: Bearer ...`) | 200 · 401 |
| GET | `/api/proveedores/{idProveedor}/metricas` | Métricas del vendedor | 200 |
| POST | `/api/lotes` | Publicar lote (calcula comisión 10 %) | 201 |
| GET | `/api/lotes` | Listar lotes publicados | 200 |

**Ejemplo — publicar lote**

```http
POST http://localhost:8080/api/lotes
Content-Type: application/json

{
  "idProveedor": 1,
  "nombreComercio": "Bodega Viña Centro",
  "nombreProducto": "Galletas integrales pack x6",
  "descripcion": "Caja sellada",
  "categoria": "ABARROTES_ENVASADOS",
  "stock": 10,
  "montoLiquido": 2000,
  "precioNormal": 4500,
  "fechaCaducidad": "2027-03-31",
  "vigenciaHoras": 6,
  "fotoBase64": null,
  "latitud": -33.0245,
  "longitud": -71.5518
}
```

Respuesta `201`: `{"id": 1, "nombreProducto": "Galletas integrales pack x6", "montoLiquido": 2000, "comision": 200, "precioOfertaFinal": 2200}`

**Ejemplo — login de proveedor**

```http
POST http://localhost:8080/api/proveedores/login
Content-Type: application/json

{ "correo": "bodega@costostock.cl", "contrasena": "Bodega2026!" }
```

### ms-transacciones — `http://localhost:8081`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/health` | Estado del servicio | 200 |
| GET | `/api/catalogo?lat=&lon=&radioKm=` | Catálogo de ofertas vigentes (filtro GPS opcional) | 200 |
| POST | `/api/reservas` | Reservar un lote por 10 minutos | 201 · 400 · 404 · 409 |
| GET | `/api/reservas/{id}` | Estado y segundos restantes de la reserva | 200 · 404 |
| DELETE | `/api/reservas/{id}` | Cancelar la reserva y devolver el stock | 200 · 409 |
| POST | `/api/pagos` | Pagar la reserva y obtener el voucher | 200 · 402 · 409 · 410 |

**Ejemplo — flujo de compra**

```http
### 1. Reservar 2 unidades del lote 1
POST http://localhost:8081/api/reservas
Content-Type: application/json

{ "idLote": 1, "cantidad": 2, "idComprador": null, "esInvitado": true }

### 2. Pagar la reserva (usar el idReserva devuelto, ej. RES-2026-1)
POST http://localhost:8081/api/pagos
Content-Type: application/json

{ "idReserva": "RES-2026-1", "idComprador": null, "metodoPago": "TARJETA", "simularFallo": false }
```

El pago devuelve `codigoVoucher` (ej. `QR-2026-1-AB12CD34`), dirección y horario de retiro, total pagado y kg de CO₂ evitados.

### Datos de prueba

**Proveedores (ms-core):**

| Correo | Contraseña | Comercio |
|---|---|---|
| `bodega@costostock.cl` | `Bodega2026!` | Bodega Viña Centro |
| `licoreria@costostock.cl` | `Puerto2026!` | Licorería El Puerto |
| `farmacia@costostock.cl` | `Farmacia2026!` | Farmacia San Juan |

**Catálogo (ms-transacciones):** 12 lotes de ejemplo en Viña del Mar, Valparaíso y Quilpué (abarrotes, congelados, farmacia, ferretería y bebidas alcohólicas).

---

## 5. Pasos para ejecutar

**Requisitos:** JDK 17 o superior. No hace falta instalar Maven: cada microservicio trae su Maven Wrapper (`mvnw`).

### Opción A — IntelliJ IDEA
1. `File → Open` y seleccionar la carpeta `ms-core`. Esperar a que Maven descargue las dependencias.
2. Ejecutar `CoreApplication` (botón ▶). Queda en `http://localhost:8080`.
3. Abrir `ms-transacciones` en otra ventana y ejecutar `TransaccionesApplication`. Queda en `http://localhost:8081`.

### Opción B — Terminal (Windows / PowerShell)

```powershell
# Terminal 1
cd ms-core
.\mvnw.cmd spring-boot:run

# Terminal 2
cd ms-transacciones
.\mvnw.cmd spring-boot:run
```

En macOS / Linux usar `./mvnw` en lugar de `.\mvnw.cmd`.

### Comprobar que están arriba
- http://localhost:8080/api/health
- http://localhost:8081/api/health
- Consola H2 de ms-core: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/coredb`, usuario `sa`, sin contraseña)

### Pruebas unitarias (JUnit 5)

```powershell
cd ms-core
.\mvnw.cmd test        # CoreServicesTest: registro de compradores y login de proveedores

cd ..\ms-transacciones
.\mvnw.cmd test        # TransaccionesServicesTest: reservas, expiración, pagos y catálogo
```

---

## 6. Capturas

### Microservicios en ejecución
![ms-core en ejecución](docs/capturas/ms-core.png)
![ms-transacciones en ejecución](docs/capturas/ms-transacciones.png)

### Endpoints probados
![Prueba de endpoints](docs/capturas/endpoints.png)

### Pruebas unitarias
![Pruebas unitarias](docs/capturas/pruebas.png)
