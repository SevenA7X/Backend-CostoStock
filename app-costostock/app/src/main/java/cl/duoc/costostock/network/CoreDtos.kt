package cl.duoc.costostock.network

import cl.duoc.costostock.model.ResumenImpactoMetricas

// JSON que la app ENVÍA a POST /api/lotes
data class PublicarLoteRequest(
    val idProveedor: Long,
    val nombreComercio: String,
    val nombreProducto: String,
    val descripcion: String,
    val categoria: String,
    val stock: Int,
    val montoLiquido: Int,
    val precioNormal: Int,
    val fechaCaducidad: String,   // yyyy-MM-dd
    val vigenciaHoras: Int,
    val fotoBase64: String?,
    val latitud: Double,
    val longitud: Double
)

// JSON que MS-Core RESPONDE al crear el lote (sólo los campos que usamos)
data class LotePublicadoDto(
    val id: Long,
    val nombreProducto: String,
    val montoLiquido: Int,
    val comision: Int,
    val precioOfertaFinal: Int
)

// JSON de GET /api/proveedores/{id}/metricas
data class MetricasVendedorDto(
    val idProveedor: Long,
    val totalLotesPublicados: Int,
    val lotesActivos: Int,
    val totalUnidadesVendidas: Int,
    val ventasBrutasTotales: Int,
    val montoLiquidoTotalAcumulado: Int,
    val comisionTotalGenerada: Int,
    val totalKgCo2Evitado: Double,
    val equivalenciaArbolesPlantados: Int
) {
    // Lo convertimos al modelo que ya usa la pantalla (contrato de datos)
    fun toDomainModel() = ResumenImpactoMetricas(
        totalKgCo2Evitado = totalKgCo2Evitado,
        equivalenciaArbolesPlantados = equivalenciaArbolesPlantados,
        totalLotesVendidosOComprados = totalUnidadesVendidas,
        montoLiquidoTotalAcumulado = montoLiquidoTotalAcumulado
    )
}
