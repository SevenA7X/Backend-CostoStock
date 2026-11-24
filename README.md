# CostoStock

Backend basado en microservicios desarrollados con Spring Boot (ms-core y ms-transacciones) para la aplicación CostoStock, un marketplace de excedentes destinado a la publicación y rescate de lotes de productos en sobrestock o próximos a vencer a precio rebajado, integrando gestión de compradores y proveedores, cálculo de comisiones, reservas temporales, emisión de vouchers QR y medición de CO₂ evitado.

---

## 1. Nombre del proyecto

**CostoStock**  
Asignatura: Desarrollo de Aplicaciones Móviles (DSY1105) · Evaluación Parcial 3

---

## 2. Integrantes

- Mario Antonio Quintero Almendras
- Romina Paz Carrasco Ruiz
- Nicolás Fernando Jaramillo Zamora
- Nicol Andreina Valero López

---


## 3. Funcionalidades

### `ms-core` (puerto `8080`)
- **Registro ligero de compradores (RF-02):** recibe nombre, correo y teléfono, valida el formato del correo y que el teléfono contenga solo dígitos (mínimo 8), y actualiza el registro conservando el mismo ID si el correo ya existe.
- **Acceso de proveedores (RF-03):** autentica mediante correo y contraseña almacenada como *hash* SHA-256, emitiendo un *token* de sesión válido por 8 horas para uso en `Authorization: Bearer <token>`.
- **Publicación de lotes (RF-05 / RF-06):** registra el lote con su imagen en Base64 y calcula automáticamente la comisión del 10 % sobre el monto líquido (`precio final = monto líquido + 10 %`).
- **Métricas del vendedor (RF-07):** consolida lotes publicados y activos, unidades, ventas brutas, monto líquido acumulado, comisión generada, kilogramos de CO₂ evitados y equivalencia anual en árboles (`21 kg CO₂ = 1 árbol/año`).
- **Verificación de estado (*health check*):** expone el estado operativo del microservicio.

### `ms-transacciones` (puerto `8081`)
- **Catálogo con filtro GPS (RF-08):** expone lotes activos, con stock disponible y vigentes, aplicando filtrado geográfico por radio (`5`, `15` o `30 km`; `30 km` por defecto) mediante la fórmula de **Haversine** cuando se envían `lat` y `lon`.
- **Reserva temporal (RF-12):** descuenta stock al crear una reserva por 10 minutos (hasta 10 unidades) y calcula subtotal, comisión de servicio del 5 %, total y CO₂ evitado.
- **Liberación automática de reservas vencidas:** ejecuta una tarea programada cada 15 segundos para devolver al stock las unidades de reservas expiradas.
- **Cancelación de reservas:** restituye el stock de reservas no pagadas al cancelarse.
- **Pago simulado y emisión de *voucher* (RF-13):** confirma la reserva, permite simular pagos rechazados y genera el código del *voucher* QR junto con los datos de retiro.
- **Estandarización de respuestas de error:** devuelve errores estructurados como `{"error": "...", "codigo": "..."}` asociados a sus respectivos códigos HTTP (`400`, `401`, `402`, `404`, `409`, `410`).

## 4. Endpoints utilizados

### API Externa
soon...

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

- JDK 17 o superior y Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Android Studio con SDK de Android (API 26+ / Android 13+ API 33 para notificaciones) y emulador o dispositivo físico con depuración USB activa

### 1. Levantar los microservicios (*backend*)

```powershell
# Terminal 1 - ms-core (puerto 8080)
cd ms-core
.\mvnw.cmd spring-boot:run

# Terminal 2 - ms-transacciones (puerto 8081)
cd ms-transacciones
.\mvnw.cmd spring-boot:run
```

En macOS / Linux, utilice `./mvnw` en lugar de `.\mvnw.cmd`.  
Verificación: `http://localhost:8080/api/health`, `http://localhost:8081/api/health` y consola H2 de `ms-core` en `http://localhost:8080/h2-console` (`jdbc:h2:file:./data/coredb`, usuario `sa`, sin contraseña).

### 2. Redirigir puertos hacia el emulador Android

```powershell
adb reverse tcp:8080 tcp:8080
adb reverse tcp:8081 tcp:8081
```

### 3. Clonar y ejecutar la aplicación móvil (*frontend*)

```bash
git clone [https://github.com/tu-usuario/costostock.git](https://github.com/tu-usuario/costostock.git)
```

1. Abra la carpeta del proyecto en **Android Studio** y espere la sincronización con Gradle.
2. Seleccione un emulador (API 26+) o dispositivo físico con depuración USB activa.
3. Ejecute la aplicación presionando `Shift + F10` o el botón **Run**.

> **Nota de permisos:** en Android 13 (API 33) o superior, el sistema solicitará permiso para **enviar notificaciones** durante la primera compra; debe aceptarse para completar el flujo.

---

## 6. Captura del APK firmado y .jks

<img width="720" height="680" alt="image" src="https://github.com/user-attachments/assets/98564ca3-c997-4ec0-bf01-7959e5dd703e" />


