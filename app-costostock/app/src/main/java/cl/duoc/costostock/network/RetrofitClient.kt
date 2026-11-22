package cl.duoc.costostock.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // apunta al localhost de su PC desde el emulador Android
    private const val BASE_URL_TRANSACCIONES = "http://localhost:8081/"

    // MS-Core (gestión de ofertas, puerto 8080).
    // 10.0.2.2 = "localhost de tu PC" visto desde el emulador de Android Studio.
    private const val BASE_URL_CORE = "http://10.0.2.2:8080/"

    val transaccionesApi: TransaccionesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_TRANSACCIONES)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TransaccionesApiService::class.java)
    }

    val coreApi: CoreApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CORE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoreApiService::class.java)
    }
}
