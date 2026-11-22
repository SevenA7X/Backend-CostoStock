package com.costostock.msproveedor.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// Error con su código HTTP: 400 (dato inválido), 404 (no existe), 503 (MS-Core no responde)...
class ApiException(val status: Int, mensaje: String) : RuntimeException(mensaje)

// Convierte las excepciones en JSON: { "error": "mensaje" } (mismo formato que MS-Core)
@RestControllerAdvice
class ManejadorErrores {

    @ExceptionHandler(ApiException::class)
    fun api(e: ApiException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(e.status).body(mapOf("error" to (e.message ?: "Error")))
}
