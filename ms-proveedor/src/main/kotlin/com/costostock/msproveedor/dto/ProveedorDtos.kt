package com.costostock.msproveedor.dto

// RF-05: mismo JSON que envía ProveedorViewModel (PublicarLoteRequest de la app)
data class PublicarLoteRequest(
    val idProveedor: Long = 0,
    val nombreComercio: String = "",
    val nombreProducto: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val stock: Int = 0,
    val montoLiquido: Int = 0,
    val precioNormal: Int = 0,
    val fechaCaducidad: String = "",   // yyyy-MM-dd
    val vigenciaHoras: Int = 0,
    val fotoBase64: String? = null,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)

// Respuesta al publicar. Incluye los campos de LotePublicadoDto de la app
// (id, nombreProducto, montoLiquido, comision, precioOfertaFinal) + datos de la foto.
data class PublicacionResponse(
    val id: Long,                  // id del lote en MS-Core
    val idPublicacion: Long,
    val nombreProducto: String,
    val montoLiquido: Int,
    val comision: Int,
    val precioOfertaFinal: Int,
    val idFoto: Long?,
    val fotoUrl: String?,
    val tamanoFotoBytes: Int,
    val fechaPublicacion: String
)

// RF-07: métricas del vendedor. Incluye los campos de MetricasVendedorDto de la app
// + datos propios de este microservicio (publicaciones y fotos).
data class MetricasProveedorResponse(
    val idProveedor: Long,
    val totalLotesPublicados: Int,
    val lotesActivos: Int,
    val totalUnidadesVendidas: Int,
    val ventasBrutasTotales: Int,
    val montoLiquidoTotalAcumulado: Int,
    val comisionTotalGenerada: Int,
    val totalKgCo2Evitado: Double,
    val equivalenciaArbolesPlantados: Int,
    val publicacionesDesdeApp: Int,
    val publicacionesConFoto: Int,
    val ultimaPublicacion: String?
)

// Resultado de validar la foto recibida
data class FotoValidada(
    val bytes: ByteArray,
    val contentType: String
)
