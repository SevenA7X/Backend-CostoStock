package com.costostock.msclima.client

import com.costostock.msclima.dto.LecturaOpenMeteo
import com.costostock.msclima.exception.ApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.databind.json.JsonMapper
import java.time.Duration

// Cliente HTTP hacia la API REST pública Open-Meteo (externa al proyecto, sin API key).
// Ejemplo: https://api.open-meteo.com/v1/forecast?latitude=-33.04&longitude=-71.62&current=temperature_2m
@Component
class OpenMeteoClient(
    @Value("\${app.open-meteo-url}") openMeteoUrl: String
) {
    companion object {
        const val VARIABLES = "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,is_day"
    }

    private val json = JsonMapper.builder().build()

    private val http: RestClient = RestClient.builder()
        .baseUrl(openMeteoUrl)
        .requestFactory(SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(5))
            setReadTimeout(Duration.ofSeconds(10))
        })
        .build()

    fun climaActual(latitud: Double, longitud: Double): LecturaOpenMeteo {
        val texto = try {
            http.get()
                .uri { uri ->
                    uri.path("/v1/forecast")
                        .queryParam("latitude", latitud)
                        .queryParam("longitude", longitud)
                        .queryParam("current", VARIABLES)
                        .queryParam("timezone", "America/Santiago")
                        .build()
                }
                .retrieve()
                .body(String::class.java)
        } catch (e: RestClientException) {
            throw ApiException(503, "No se pudo consultar Open-Meteo. Revisa la conexión a internet.")
        } ?: throw ApiException(503, "Open-Meteo respondió vacío")

        // Se lee el bloque "current" de la respuesta
        val actual = json.readTree(texto).path("current")
        if (actual.isMissingNode) throw ApiException(503, "Open-Meteo no devolvió el clima actual")

        return LecturaOpenMeteo(
            temperatura = actual.path("temperature_2m").asDouble(),
            humedad = actual.path("relative_humidity_2m").asInt(),
            viento = actual.path("wind_speed_10m").asDouble(),
            codigoClima = actual.path("weather_code").asInt(),
            esDeDia = actual.path("is_day").asInt() == 1,
            horaMedicion = actual.path("time").toString().trim('"')
        )
    }
}
