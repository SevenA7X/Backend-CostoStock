package com.costostock.msclima.service

import com.costostock.msclima.client.OpenMeteoClient
import com.costostock.msclima.dto.CiudadResponse
import com.costostock.msclima.dto.ClimaResponse
import com.costostock.msclima.exception.ApiException
import com.costostock.msclima.model.Ciudad
import com.costostock.msclima.model.ConsultaClima
import com.costostock.msclima.repository.ConsultaClimaRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ClimaService(
    private val openMeteo: OpenMeteoClient,
    private val consultas: ConsultaClimaRepository,
    @Value("\${app.cache-minutos}") private val cacheMinutos: Long
) {
    fun ciudades(): List<CiudadResponse> =
        Ciudad.entries.map { CiudadResponse(it.name, it.nombre, it.latitud, it.longitud) }

    // Clima de una ciudad. Si hay una consulta reciente (caché) se reutiliza;
    // si Open-Meteo falla, se devuelve la última consulta guardada con un aviso.
    @Transactional
    fun climaDe(codigoCiudad: String): ClimaResponse {
        val ciudad = Ciudad.entries.firstOrNull { it.name.equals(codigoCiudad.trim(), ignoreCase = true) }
            ?: throw ApiException(400, "Ciudad no válida. Usa: ${Ciudad.entries.joinToString { it.name }}")

        val ultima = consultas.findFirstByCiudadOrderByFechaConsultaDesc(ciudad.name)
        if (ultima != null && ultima.fechaConsulta.isAfter(LocalDateTime.now().minusMinutes(cacheMinutos))) {
            return aRespuesta(ultima, ciudad.nombre, desdeCache = true)
        }

        return try {
            val nueva = consultar(ciudad.name, ciudad.latitud, ciudad.longitud)
            aRespuesta(nueva, ciudad.nombre)
        } catch (e: ApiException) {
            if (ultima == null) throw e
            aRespuesta(
                ultima, ciudad.nombre, desdeCache = true,
                aviso = "Open-Meteo no respondió: se muestra la última medición guardada"
            )
        }
    }

    // Clima de cualquier punto (por ejemplo, la ubicación de una tienda)
    @Transactional
    fun climaPorCoordenadas(latitud: Double, longitud: Double): ClimaResponse {
        if (latitud !in -90.0..90.0 || longitud !in -180.0..180.0) {
            throw ApiException(400, "Coordenadas fuera de rango")
        }
        return aRespuesta(consultar("PERSONALIZADA", latitud, longitud), "Ubicación personalizada")
    }

    @Transactional(readOnly = true)
    fun historial(codigoCiudad: String): List<ConsultaClima> =
        consultas.findTop20ByCiudadOrderByFechaConsultaDesc(codigoCiudad.trim().uppercase())

    private fun consultar(ciudad: String, latitud: Double, longitud: Double): ConsultaClima {
        val lectura = openMeteo.climaActual(latitud, longitud)
        return consultas.save(
            ConsultaClima(
                ciudad = ciudad,
                latitud = latitud,
                longitud = longitud,
                temperatura = lectura.temperatura,
                humedad = lectura.humedad,
                viento = lectura.viento,
                codigoClima = lectura.codigoClima,
                esDeDia = lectura.esDeDia,
                horaMedicion = lectura.horaMedicion
            )
        )
    }

    private fun aRespuesta(
        c: ConsultaClima, nombre: String, desdeCache: Boolean = false, aviso: String? = null
    ): ClimaResponse {
        val (descripcion, icono) = ClimaTraductor.describir(c.codigoClima, c.esDeDia)
        return ClimaResponse(
            ciudad = nombre,
            latitud = c.latitud,
            longitud = c.longitud,
            temperatura = c.temperatura,
            humedad = c.humedad,
            vientoKmH = c.viento,
            descripcion = descripcion,
            icono = icono,
            esDeDia = c.esDeDia,
            horaMedicion = c.horaMedicion.substringAfter('T'),
            consejoEcologico = ClimaTraductor.consejo(c.temperatura),
            desdeCache = desdeCache,
            aviso = aviso
        )
    }
}
