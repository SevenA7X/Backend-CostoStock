package com.costostock.mstransacciones.controller

import com.costostock.mstransacciones.model.LoteOferta
import com.costostock.mstransacciones.service.CatalogoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/catalogo")
@CrossOrigin(origins = ["*"])
class CatalogoController(
    private val catalogoService: CatalogoService
) {
    @GetMapping
    fun listarCatalogo(
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lon: Double?,
        @RequestParam(defaultValue = "30.0") radioKm: Double
    ): ResponseEntity<List<LoteOferta>> {
        return ResponseEntity.ok(catalogoService.obtenerCatalogoActivo(lat, lon, radioKm))
    }
}