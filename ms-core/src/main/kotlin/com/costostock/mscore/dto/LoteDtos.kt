package com.costostock.mscore.dto

// Lo que envía la app al publicar o editar un lote (JSON)
data class LoteRequest(
    val idProveedor: Long = 0,
    val nombreComercio: String = "",
    val nombreProducto: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val stock: Int = 0,
    val montoLiquido: Int = 0,
    val precioNormal: Int = 0,
    val fechaCaducidad: String = "",   // formato ISO: yyyy-MM-dd
    val vigenciaHoras: Int = 0,
    val fotoBase64: String? = null,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)

// RF-06: resultado del cálculo de comisión que hace el controlador
data class DesglosePrecio(
    val montoLiquido: Int,
    val comision: Int,
    val precioFinal: Int
)

data class VentaRequest(
    val cantidad: Int = 1
)

// RF-07: datos de ventas que consume la vista de métricas del vendedor
data class MetricasVendedorResponse(
    val idProveedor: Long,
    val totalLotesPublicados: Int,
    val lotesActivos: Int,
    val totalUnidadesVendidas: Int,
    val ventasBrutasTotales: Int,
    val montoLiquidoTotalAcumulado: Int,
    val comisionTotalGenerada: Int,
    val totalKgCo2Evitado: Double,
    val equivalenciaArbolesPlantados: Int
)
