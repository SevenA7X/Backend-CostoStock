package com.costostock.mscore.controller

import com.costostock.mscore.dto.MetricasVendedorResponse
import com.costostock.mscore.service.LoteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = ["*"])
class MetricasController(
    private val loteService: LoteService
) {
    // GET /api/proveedores/1/metricas  (RF-07)
    @GetMapping("/{idProveedor}/metricas")
    fun metricas(@PathVariable idProveedor: Long): ResponseEntity<MetricasVendedorResponse> =
        ResponseEntity.ok(loteService.metricasDeProveedor(idProveedor))
}
