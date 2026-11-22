package cl.duoc.costostock.network.externa

import com.google.gson.annotations.SerializedName

// Respuesta de la API pública Open-Meteo (https://open-meteo.com)
data class ClimaRespuestaDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val current: ClimaActualDto?
)

data class ClimaActualDto(
    val time: String,
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("relative_humidity_2m") val humedad: Int,
    @SerializedName("wind_speed_10m") val viento: Double,
    @SerializedName("weather_code") val codigoClima: Int,
    @SerializedName("is_day") val esDeDia: Int
)
