package cl.duoc.costostock.network.externa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Instancia de Retrofit SEPARADA de RetrofitClient (que apunta a Spring Boot).
// Así la API externa queda desacoplada de los microservicios.
object ClimaRetrofitClient {
    private const val BASE_URL_OPEN_METEO = "https://api.open-meteo.com/"

    val api: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_OPEN_METEO)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }
}
