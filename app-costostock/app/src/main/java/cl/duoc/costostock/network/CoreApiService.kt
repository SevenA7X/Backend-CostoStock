package cl.duoc.costostock.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Endpoints de MS-Core (Spring Boot, puerto 8080)
interface CoreApiService {

    // RF-05: envía el lote completo (incluida la foto en Base64)
    @POST("api/lotes")
    suspend fun publicarLote(@Body lote: PublicarLoteRequest): Response<LotePublicadoDto>

    // RF-07: datos de ventas del vendedor
    @GET("api/proveedores/{idProveedor}/metricas")
    suspend fun obtenerMetricas(@Path("idProveedor") idProveedor: Long): MetricasVendedorDto
}
