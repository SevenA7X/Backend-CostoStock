package com.costostock.mscore.service

import com.costostock.mscore.dto.DesglosePrecio
import com.costostock.mscore.dto.LoteRequest
import com.costostock.mscore.dto.MetricasVendedorResponse
import com.costostock.mscore.exception.LoteNoEncontradoException
import com.costostock.mscore.exception.ReglaNegocioException
import com.costostock.mscore.model.Lote
import com.costostock.mscore.repository.LoteRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

@Service
class LoteService(
    private val repository: LoteRepository
) {
    companion object {
        // Categorías permitidas (mismas que el enum CategoriaProducto de la app)
        val CATEGORIAS_PERMITIDAS = listOf(
            "ABARROTES_ENVASADOS", "CONGELADOS", "FERRETERIA", "FARMACIA", "BEBIDAS_ALCOHOLICAS"
        )

        // RF-04: palabras que identifican alimentos frescos (misma lista que la app)
        val PALABRAS_FRESCAS = listOf(
            "fruta", "verdura", "carne", "pollo", "pescado", "marisco",
            "lechuga", "tomate", "palta", "fresco", "fresca"
        )

        // Kg de CO2 evitados por unidad según categoría (valores referenciales)
        val CO2_POR_CATEGORIA = mapOf(
            "ABARROTES_ENVASADOS" to 1.2,
            "CONGELADOS" to 2.0,
            "BEBIDAS_ALCOHOLICAS" to 0.8,
            "FARMACIA" to 0.3,
            "FERRETERIA" to 0.5
        )

        const val KG_CO2_POR_ARBOL_AL_ANIO = 21.0
    }

    // ---------- LECTURA ----------
    fun listar(idProveedor: Long?): List<Lote> =
        if (idProveedor != null) repository.findByIdProveedor(idProveedor) else repository.findAll()

    fun buscarPorId(id: Long): Lote =
        repository.findById(id).orElseThrow { LoteNoEncontradoException(id) }

    // ---------- CREAR ----------
    fun crear(req: LoteRequest, precio: DesglosePrecio): Lote {
        validar(req, precio)
        val ahora = LocalDateTime.now()
        val categoria = req.categoria.uppercase()
        val lote = Lote(
            idProveedor = req.idProveedor,
            nombreComercio = req.nombreComercio,
            nombreProducto = req.nombreProducto.trim(),
            descripcion = req.descripcion.trim(),
            categoria = categoria,
            stock = req.stock,
            montoLiquido = precio.montoLiquido,
            comision = precio.comision,
            precioOfertaFinal = precio.precioFinal,
            precioNormal = req.precioNormal,
            fechaCaducidad = convertirFecha(req.fechaCaducidad),
            finOferta = ahora.plusHours(req.vigenciaHoras.toLong()),
            fechaPublicacion = ahora,
            fechaCapturaFoto = if (req.fotoBase64 != null) ahora else null,
            fotoBase64 = req.fotoBase64,
            kgCo2EvitadoPorUnidad = CO2_POR_CATEGORIA[categoria] ?: 1.0,
            latitud = req.latitud,
            longitud = req.longitud,
            esAlcohol = categoria == "BEBIDAS_ALCOHOLICAS",
            activo = true
        )
        return repository.save(lote)
    }

    // ---------- ACTUALIZAR ----------
    fun actualizar(id: Long, req: LoteRequest, precio: DesglosePrecio): Lote {
        val lote = buscarPorId(id)
        validar(req, precio)
        val categoria = req.categoria.uppercase()
        lote.nombreProducto = req.nombreProducto.trim()
        lote.descripcion = req.descripcion.trim()
        lote.categoria = categoria
        lote.stock = req.stock
        lote.montoLiquido = precio.montoLiquido
        lote.comision = precio.comision
        lote.precioOfertaFinal = precio.precioFinal
        lote.precioNormal = req.precioNormal
        lote.fechaCaducidad = convertirFecha(req.fechaCaducidad)
        lote.finOferta = LocalDateTime.now().plusHours(req.vigenciaHoras.toLong())
        lote.kgCo2EvitadoPorUnidad = CO2_POR_CATEGORIA[categoria] ?: 1.0
        lote.esAlcohol = categoria == "BEBIDAS_ALCOHOLICAS"
        if (req.fotoBase64 != null) {
            lote.fotoBase64 = req.fotoBase64
            lote.fechaCapturaFoto = LocalDateTime.now()
        }
        return repository.save(lote)
    }

    // ---------- ELIMINAR ----------
    fun eliminar(id: Long) {
        val lote = buscarPorId(id)
        repository.delete(lote)
    }

    // ---------- VENTAS (alimenta RF-07) ----------
    fun registrarVenta(id: Long, cantidad: Int): Lote {
        val lote = buscarPorId(id)
        if (cantidad <= 0) throw ReglaNegocioException("La cantidad vendida debe ser mayor a 0")
        if (cantidad > lote.stock) throw ReglaNegocioException("Stock insuficiente: quedan ${lote.stock} unidades")
        lote.stock -= cantidad
        lote.unidadesVendidas += cantidad
        if (lote.stock == 0) lote.activo = false
        return repository.save(lote)
    }

    // ---------- MÉTRICAS DEL VENDEDOR (RF-07) ----------
    fun metricasDeProveedor(idProveedor: Long): MetricasVendedorResponse {
        val lotes = repository.findByIdProveedor(idProveedor)
        val unidades = lotes.sumOf { it.unidadesVendidas }
        val kgCo2 = lotes.sumOf { it.unidadesVendidas * it.kgCo2EvitadoPorUnidad }
        return MetricasVendedorResponse(
            idProveedor = idProveedor,
            totalLotesPublicados = lotes.size,
            lotesActivos = lotes.count { it.activo },
            totalUnidadesVendidas = unidades,
            ventasBrutasTotales = lotes.sumOf { it.unidadesVendidas * it.precioOfertaFinal },
            montoLiquidoTotalAcumulado = lotes.sumOf { it.unidadesVendidas * it.montoLiquido },
            comisionTotalGenerada = lotes.sumOf { it.unidadesVendidas * it.comision },
            totalKgCo2Evitado = kgCo2,
            equivalenciaArbolesPlantados = (kgCo2 / KG_CO2_POR_ARBOL_AL_ANIO).roundToInt()
        )
    }

    // ---------- VALIDACIONES (RF-04) ----------
    private fun validar(req: LoteRequest, precio: DesglosePrecio) {
        if (req.nombreProducto.isBlank()) throw ReglaNegocioException("El nombre del producto es obligatorio")
        if (esAlimentoFresco(req.nombreProducto) || esAlimentoFresco(req.descripcion)) {
            throw ReglaNegocioException("RF-04: CostoStock no permite publicar alimentos frescos")
        }
        if (req.categoria.uppercase() !in CATEGORIAS_PERMITIDAS) {
            throw ReglaNegocioException("Categoría no permitida. Use: ${CATEGORIAS_PERMITIDAS.joinToString()}")
        }
        if (req.stock <= 0) throw ReglaNegocioException("El stock debe ser mayor a 0")
        if (req.montoLiquido <= 0) throw ReglaNegocioException("El monto líquido debe ser mayor a 0")
        if (req.precioNormal <= precio.precioFinal) {
            throw ReglaNegocioException("El precio normal debe ser mayor al precio de oferta (${precio.precioFinal})")
        }
        if (req.vigenciaHoras <= 0) throw ReglaNegocioException("La vigencia debe ser mayor a 0 horas")
        if (!convertirFecha(req.fechaCaducidad).isAfter(LocalDateTime.now())) {
            throw ReglaNegocioException("La fecha de caducidad debe ser futura")
        }
    }

    private fun esAlimentoFresco(texto: String): Boolean {
        val t = texto.lowercase()
        return PALABRAS_FRESCAS.any { t.contains(it) }
    }

    private fun convertirFecha(iso: String): LocalDateTime = try {
        LocalDate.parse(iso).atTime(23, 59)
    } catch (e: DateTimeParseException) {
        throw ReglaNegocioException("Fecha de caducidad inválida, use el formato yyyy-MM-dd")
    }
}
