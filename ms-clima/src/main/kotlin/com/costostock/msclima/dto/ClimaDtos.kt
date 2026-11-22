package com.costostock.msclima.dto

// Respuesta lista para mostrar (mismos datos que ClimaUi de la app)
data class ClimaResponse(
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val temperatura: Double,
    val humedad: Int,
    val vientoKmH: Double,
    val descripcion: String,
    val icono: String,
    val esDeDia: Boolean,
    val horaMedicion: String,
    val consejoEcologico: String,
    val fuente: String = "Open-Meteo (https://open-meteo.com) · CC BY 4.0",
    val desdeCache: Boolean = false,
    val aviso: String? = null
)

data class CiudadResponse(
    val codigo: String,
    val nombre: String,
    val latitud: Double,
    val longitud: Double
)

// Datos leídos desde la respuesta JSON de Open-Meteo
data class LecturaOpenMeteo(
    val temperatura: Double,
    val humedad: Int,
    val viento: Double,
    val codigoClima: Int,
    val esDeDia: Boolean,
    val horaMedicion: String
)
