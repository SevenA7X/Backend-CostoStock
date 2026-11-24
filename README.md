# CostoStock

Aplicación móvil y arquitectura de microservicios para la publicación y rescate de lotes de productos en sobrestock o próximos a vencer, con precio rebajado y cálculo del CO₂ evitado.

---

## 1. Nombre del proyecto

**CostoStock**  
Asignatura: Desarrollo de Aplicaciones Móviles (DSY1105) · Evaluación Parcial 3 / 4

---

## 2. Integrantes

| Integrante | Correo | Rol / Aporte |
|---|---|---|
| Mario Antonio Quintero Almendras | ma.quinteroa@duocuc.cl | Arquitectura del proyecto, aplicación móvil Android (Kotlin / Jetpack Compose) y microservicios |
| Romina Paz Carrasco Ruiz | ro.carrascor@duocuc.cl | Desarrollo frontend/backend, reservas temporales, pagos simulados y registro de compradores |
| Nicolás Fernando Jaramillo Zamora | ni.jaramilloz@duocuc.cl | Gestión de vouchers QR e historial |
| Nicol Andreina Valero López | nic.valero@duocuc.cl | Publicación de lotes, cálculo de comisión (10 %) y métricas |

---

## 3. Funcionalidades

- **Catálogo de ofertas con filtro GPS (RF-08):** visualización de lotes activos con stock disponible y fecha vigente, filtrado por proximidad geográfica (radios de 5, 15 y 30 km mediante fórmula de Haversine).
- **Registro de compradores y acceso de proveedores (RF-02 / RF-03):** registro ligero de compradores y autenticación de proveedores con contraseña cifrada en SHA-256 y sesión mediante token Bearer.
- **Publicación de lotes y cálculo de comisión (RF-05 / RF-06):** creación de ofertas con fotografía, stock, vigencia y cálculo automático de comisión del 10 % (`precio final = monto líquido + 10 %`).
- **Reserva temporal con liberación automática (RF-12):** bloqueo de stock por 10 minutos con tarea programada que libera unidades al expirar el tiempo.
- **Pago simulado y emisión de voucher QR (RF-13 / RF-14):** confirmación de compra, generación de código QR de retiro y validación de entrega.
- **Métricas de impacto ambiental (RF-07):** cálculo de kg de CO₂ evitados y su equivalencia en árboles plantados.

---

## 4. Endpoints utilizados

### API Externa
- **Servicio:** *[Completar con la URL base y endpoint de la API externa consumida vía Retrofit en la app Android, por ejemplo Open-Meteo / Nominatim / API pública utilizada]*
  - `GET <URL_ENDPOINT_EXTERNO>` — *[Descripción breve de qué dato obtiene y dónde se muestra en la app]*

### Microservicio `ms-core` (`http://localhost:8080`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/health` | Estado del servicio |
| `POST` | `/api/compradores/registro` | Registro ligero de comprador |
| `GET` | `/api/compradores/{id}` | Consulta de comprador por ID |
| `POST` | `/api/proveedores/login` | Autenticación de proveedor y emisión de token |
| `GET` | `/api/proveedores/acceso` | Validación de token `Authorization: Bearer` |
| `GET` | `/api/proveedores/{idProveedor}/metricas` | Métricas de ventas e impacto ambiental del proveedor |
| `POST` | `/api/lotes` | Publicación de lote con cálculo de comisión (10 %) |
| `GET` | `/api/lotes` | Listado de lotes publicados |
| `GET` | `/api/lotes/{id}` | Consulta de lote por ID |
| `PUT` | `/api/lotes/{id}` | Actualización de lote |
| `DELETE` | `/api/lotes/{id}` | Eliminación de lote |
| `PATCH` | `/api/lotes/{id}/ventas` | Descuento de stock por venta registrada |

### Microservicio `ms-transacciones` (`http://localhost:8081`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/health` | Estado del servicio |
| `GET` | `/api/catalogo` | Listado de ofertas vigentes con filtro GPS (`lat`, `lon`, `radioKm`) |
| `POST` | `/api/reservas` | Creación de reserva temporal por 10 minutos |
| `GET` | `/api/reservas/{id}` | Consulta de estado y tiempo restante de reserva |
| `DELETE` | `/api/reservas/{id}` | Cancelación de reserva y devolución de stock |
| `POST` | `/api/pagos` | Procesamiento de pago simulado y emisión de voucher QR |

---

## 5. Pasos para ejecutar

### Requisitos previos
- JDK 21 (o JDK 17+)
- Android Studio con SDK de Android y emulador o dispositivo físico configurado

### 1. Levantar los microservicios (Backend)

```powershell
# Terminal 1 - ms-core (puerto 8080)
cd ms-core
.\mvnw.cmd spring-boot:run

# Terminal 2 - ms-transacciones (puerto 8081)
cd ms-transacciones
.\mvnw.cmd spring-boot:run
