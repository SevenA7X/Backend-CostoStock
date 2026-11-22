package com.costostock.msclima.model

import jakarta.persistence.*
import java.time.LocalDateTime

// Ciudades disponibles (las mismas que la pantalla Clima de la app)
enum class Ciudad(val nombre: String, val latitud: Double, val longitud: Double) {
    VALPARAISO("Valparaíso", -33.0458, -71.6197),
    VINA_DEL_MAR("Viña del Mar", -33.0245, -71.5518),
    SANTIAGO("Santiago", -33.4489, -70.6693)
}

// Cada consulta hecha a Open-Meteo queda guardada (tabla consultas_clima):
// sirve de caché, de respaldo si la API externa falla y de historial.
@Entity
@Table(name = "consultas_clima")
data class ConsultaClima(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var ciudad: String = "",            // VALPARAISO, SANTIAGO... o PERSONALIZADA
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var temperatura: Double = 0.0,      // °C
    var humedad: Int = 0,               // %
    var viento: Double = 0.0,           // km/h
    var codigoClima: Int = 0,           // código WMO
    var esDeDia: Boolean = true,
    var horaMedicion: String = "",      // hora que informa Open-Meteo (ej. 2026-11-22T15:00)
    var fechaConsulta: LocalDateTime = LocalDateTime.now()
)
