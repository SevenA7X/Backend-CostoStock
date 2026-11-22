package cl.duoc.costostock.network.externa

import retrofit2.http.GET
import retrofit2.http.Query

// API REST AJENA al proyecto (no pasa por los microservicios Spring Boot)
interface ClimaApiService {

    // Ejemplo: /v1/forecast?latitude=-33.04&longitude=-71.62&current=temperature_2m,...
    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") variables: String =
            "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,is_day",
        @Query("timezone") zonaHoraria: String = "America/Santiago"
    ): ClimaRespuestaDto
}
