package com.costostock.mscore.controller

import com.costostock.mscore.dto.DesglosePrecio
import com.costostock.mscore.dto.LoteRequest
import com.costostock.mscore.dto.VentaRequest
import com.costostock.mscore.model.Lote
import com.costostock.mscore.service.LoteService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.math.roundToInt

@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = ["*"])
class LoteController(
    private val loteService: LoteService
) {
    companion object {
        // RF-06: comisión de CostoStock
        const val PORCENTAJE_COMISION = 0.10

        // RF-06: calcula el 10% y lo SUMA al monto líquido del proveedor
        fun calcularPrecioFinal(montoLiquido: Int): DesglosePrecio {
            val comision = (montoLiquido * PORCENTAJE_COMISION).roundToInt()
            return DesglosePrecio(
                montoLiquido = montoLiquido,
                comision = comision,
                precioFinal = montoLiquido + comision
            )
        }
    }

    // GET /api/lotes            -> todos
    // GET /api/lotes?idProveedor=1 -> sólo los de un proveedor
    @GetMapping
    fun listar(@RequestParam(required = false) idProveedor: Long?): ResponseEntity<List<Lote>> =
        ResponseEntity.ok(loteService.listar(idProveedor))

    // GET /api/lotes/{id}
    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): ResponseEntity<Lote> =
        ResponseEntity.ok(loteService.buscarPorId(id))

    // POST /api/lotes  (RF-04 + RF-06)
    @PostMapping
    fun crear(@RequestBody request: LoteRequest): ResponseEntity<Lote> {
        val precio = calcularPrecioFinal(request.montoLiquido)
        val creado = loteService.crear(request, precio)
        return ResponseEntity.status(HttpStatus.CREATED).body(creado)
    }

    // PUT /api/lotes/{id}
    @PutMapping("/{id}")
    fun actualizar(@PathVariable id: Long, @RequestBody request: LoteRequest): ResponseEntity<Lote> {
        val precio = calcularPrecioFinal(request.montoLiquido)
        return ResponseEntity.ok(loteService.actualizar(id, request, precio))
    }

    // DELETE /api/lotes/{id}
    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long): ResponseEntity<Void> {
        loteService.eliminar(id)
        return ResponseEntity.noContent().build()
    }

    // GET /api/lotes/simular-precio?montoLiquido=5000  -> muestra el desglose RF-06
    @GetMapping("/simular-precio")
    fun simularPrecio(@RequestParam montoLiquido: Int): ResponseEntity<DesglosePrecio> =
        ResponseEntity.ok(calcularPrecioFinal(montoLiquido))

    // PATCH /api/lotes/{id}/ventas  body: { "cantidad": 2 }
    @PatchMapping("/{id}/ventas")
    fun registrarVenta(@PathVariable id: Long, @RequestBody venta: VentaRequest): ResponseEntity<Lote> =
        ResponseEntity.ok(loteService.registrarVenta(id, venta.cantidad))
}
