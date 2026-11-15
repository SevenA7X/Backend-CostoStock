package com.costostock.mstransacciones.service

import com.costostock.mstransacciones.model.LoteOferta
import com.costostock.mstransacciones.repository.LoteOfertaRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.math.*

@Service
class CatalogoService(
    private val repository: LoteOfertaRepository
) {
    fun obtenerCatalogoActivo(latUsuario: Double?, lonUsuario: Double?, radioKm: Double?): List<LoteOferta> {
        val ahora = LocalDateTime.now()
        val lotes = repository.findByActivoTrueAndStockGreaterThan(0)
        val resultado = mutableListOf<LoteOferta>()

        for (lote in lotes) {
            // RF-11: Baja automática por caducidad
            if (lote.fechaCaducidad != null && !ahora.isBefore(lote.fechaCaducidad)) {
                lote.activo = false
                repository.save(lote)
                continue
            }

            // RF-10: Reversión automática de precio al vencer la oferta
            if (lote.finOferta != null && !ahora.isBefore(lote.finOferta)) {
                if (lote.precioOferta != lote.precioNormal) {
                    lote.precioOferta = lote.precioNormal
                    repository.save(lote)
                }
            }

            // RF-08: Filtrado por radio de distancia (5, 15 o 30 km)
            if (latUsuario != null && lonUsuario != null && radioKm != null) {
                val distancia = calcularDistanciaKm(latUsuario, lonUsuario, lote.latitud, lote.longitud)
                if (distancia <= radioKm) {
                    resultado.add(lote)
                }
            } else {
                resultado.add(lote)
            }
        }
        return resultado
    }

    private fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}