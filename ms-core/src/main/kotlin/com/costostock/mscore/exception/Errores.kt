package com.costostock.mscore.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// Se lanza cuando una regla de negocio no se cumple (ej. alimento fresco)
class ReglaNegocioException(mensaje: String) : RuntimeException(mensaje)

// Se lanza cuando el lote buscado no existe
class LoteNoEncontradoException(id: Long) : RuntimeException("No existe el lote con id $id")

// Convierte las excepciones en respuestas JSON: { "error": "mensaje" }
@RestControllerAdvice
class ManejadorErrores {

    @ExceptionHandler(ReglaNegocioException::class)
    fun reglaNegocio(e: ReglaNegocioException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to (e.message ?: "Solicitud inválida")))

    @ExceptionHandler(LoteNoEncontradoException::class)
    fun noEncontrado(e: LoteNoEncontradoException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (e.message ?: "No encontrado")))
}
