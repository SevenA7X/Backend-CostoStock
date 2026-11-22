package com.costostock.msproveedor.service

import com.costostock.msproveedor.client.MsCoreClient
import com.costostock.msproveedor.client.texto
import com.costostock.msproveedor.dto.MetricasProveedorResponse
import com.costostock.msproveedor.dto.PublicacionResponse
import com.costostock.msproveedor.dto.PublicarLoteRequest
import com.costostock.msproveedor.exception.ApiException
import com.costostock.msproveedor.model.FotoLote
import com.costostock.msproveedor.model.Publicacion
import com.costostock.msproveedor.repository.FotoLoteRepository
import com.costostock.msproveedor.repository.PublicacionRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProveedorService(
    private val msCore: MsCoreClient,
    private val fotos: FotoLoteRepository,
    private val publicaciones: PublicacionRepository,
    @Value("\${app.foto-max-bytes}") private val fotoMaxBytes: Int
) {

    // ---------- RF-05: publicar el lote con su foto nativa ----------
    @Transactional
    fun publicar(req: PublicarLoteRequest): PublicacionResponse {
        if (req.idProveedor <= 0) throw ApiException(400, "Falta el id del proveedor")
        val fotoBase64 = req.fotoBase64
            ?: throw ApiException(400, "RF-05: el lote debe incluir la foto actual tomada con la cámara")

        // 1) Se valida la foto aquí (es un JPEG/PNG real y no supera el tamaño máximo)
        val foto = FotoValidator.validar(fotoBase64, fotoMaxBytes)

        // 2) MS-Core valida el lote (RF-04) y calcula la comisión (RF-06). Si lo rechaza, no se guarda nada.
        val lote = msCore.publicarLote(req)
        val idLote = lote.path("id").asLong()

        // 3) Se guardan la foto y el registro de la publicación en la base de este microservicio
        val fotoGuardada = fotos.save(
            FotoLote(
                idProveedor = req.idProveedor,
                idLote = idLote,
                contentType = foto.contentType,
                tamanoBytes = foto.bytes.size,
                datos = foto.bytes
            )
        )
        val publicacion = publicaciones.save(
            Publicacion(
                idProveedor = req.idProveedor,
                idLote = idLote,
                nombreProducto = lote.path("nombreProducto").texto() ?: req.nombreProducto,
                categoria = lote.path("categoria").texto() ?: req.categoria,
                montoLiquido = lote.path("montoLiquido").asInt(),
                comision = lote.path("comision").asInt(),
                precioOfertaFinal = lote.path("precioOfertaFinal").asInt(),
                idFoto = fotoGuardada.id
            )
        )

        return PublicacionResponse(
            id = idLote,
            idPublicacion = publicacion.id!!,
            nombreProducto = publicacion.nombreProducto,
            montoLiquido = publicacion.montoLiquido,
            comision = publicacion.comision,
            precioOfertaFinal = publicacion.precioOfertaFinal,
            idFoto = fotoGuardada.id,
            fotoUrl = "/api/proveedor/fotos/${fotoGuardada.id}",
            tamanoFotoBytes = fotoGuardada.tamanoBytes,
            fechaPublicacion = publicacion.fechaPublicacion.toString()
        )
    }

    @Transactional(readOnly = true)
    fun foto(id: Long): FotoLote =
        fotos.findById(id).orElseThrow { ApiException(404, "No existe la foto con id $id") }

    @Transactional(readOnly = true)
    fun publicacionesDe(idProveedor: Long): List<Publicacion> =
        publicaciones.findByIdProveedorOrderByIdDesc(idProveedor)

    // ---------- RF-07: métricas de ventas del vendedor ----------
    @Transactional(readOnly = true)
    fun metricas(idProveedor: Long): MetricasProveedorResponse {
        val ventas = msCore.metricas(idProveedor)          // datos de ventas reales (MS-Core)
        val propias = publicaciones.findByIdProveedorOrderByIdDesc(idProveedor)

        return MetricasProveedorResponse(
            idProveedor = idProveedor,
            totalLotesPublicados = ventas.path("totalLotesPublicados").asInt(),
            lotesActivos = ventas.path("lotesActivos").asInt(),
            totalUnidadesVendidas = ventas.path("totalUnidadesVendidas").asInt(),
            ventasBrutasTotales = ventas.path("ventasBrutasTotales").asInt(),
            montoLiquidoTotalAcumulado = ventas.path("montoLiquidoTotalAcumulado").asInt(),
            comisionTotalGenerada = ventas.path("comisionTotalGenerada").asInt(),
            totalKgCo2Evitado = ventas.path("totalKgCo2Evitado").asDouble(),
            equivalenciaArbolesPlantados = ventas.path("equivalenciaArbolesPlantados").asInt(),
            publicacionesDesdeApp = propias.size,
            publicacionesConFoto = propias.count { it.idFoto != null },
            ultimaPublicacion = propias.firstOrNull()?.fechaPublicacion?.toString()
        )
    }
}
