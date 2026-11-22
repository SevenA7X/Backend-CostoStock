package com.costostock.msproveedor.client

import com.costostock.msproveedor.dto.PublicarLoteRequest
import com.costostock.msproveedor.exception.ApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.time.Duration

// Cliente HTTP hacia MS-Core (puerto 8080). Aquí ocurre la comunicación entre microservicios:
// ms-proveedor no tiene la tabla de lotes, le PIDE a MS-Core que publique y que entregue las ventas.
@Component
class MsCoreClient(
    @Value("\${app.ms-core-url}") msCoreUrl: String
) {
    private val json = JsonMapper.builder().build()

    private val http: RestClient = RestClient.builder()
        .baseUrl(msCoreUrl)
        .requestFactory(SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(3))
            setReadTimeout(Duration.ofSeconds(10))
        })
        .build()

    // POST /api/lotes en MS-Core: valida RF-04 (sin alimentos frescos) y calcula RF-06 (10 %)
    fun publicarLote(request: PublicarLoteRequest): JsonNode = llamar {
        http.post()
            .uri("/api/lotes")
            .contentType(MediaType.APPLICATION_JSON)
            .body(json.writeValueAsString(request))
            .retrieve()
            .body(String::class.java)
    }

    // GET /api/proveedores/{id}/metricas en MS-Core: datos de ventas (RF-07)
    fun metricas(idProveedor: Long): JsonNode = llamar {
        http.get()
            .uri("/api/proveedores/{id}/metricas", idProveedor)
            .retrieve()
            .body(String::class.java)
    }

    private fun llamar(peticion: () -> String?): JsonNode {
        try {
            val texto = peticion() ?: throw ApiException(502, "MS-Core respondió vacío")
            return json.readTree(texto)
        } catch (e: RestClientResponseException) {
            // MS-Core respondió con error: se reenvía su mensaje { "error": "..." }
            val status = e.statusCode.value()
            val mensaje = leerError(e.responseBodyAsString) ?: "MS-Core rechazó la solicitud"
            throw ApiException(if (status == 400 || status == 404) status else 502, mensaje)
        } catch (e: ResourceAccessException) {
            throw ApiException(503, "MS-Core (puerto 8080) no está disponible. Inícialo e intenta nuevamente.")
        }
    }

    private fun leerError(cuerpo: String): String? = try {
        json.readTree(cuerpo).path("error").texto()
    } catch (e: Exception) {
        null
    }
}

// Lee un texto de un nodo JSON (funciona igual en Jackson 2 y 3)
fun JsonNode.texto(): String? =
    if (isMissingNode || isNull) null else toString().trim('"')
