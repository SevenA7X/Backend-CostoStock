package com.costostock.msclima.controller

import com.costostock.msclima.dto.CiudadResponse
import com.costostock.msclima.dto.ClimaResponse
import com.costostock.msclima.model.ConsultaClima
import com.costostock.msclima.service.ClimaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clima")
@CrossOrigin(origins = ["*"])
class ClimaController(
    private val service: ClimaService
) {
    // GET /api/clima?ciudad=VALPARAISO   (por defecto Valparaíso)
    @GetMapping
    fun clima(@RequestParam(defaultValue = "VALPARAISO") ciudad: String): ResponseEntity<ClimaResponse> =
        ResponseEntity.ok(service.climaDe(ciudad))

    // GET /api/clima/coordenadas?lat=-33.04&lon=-71.62
    @GetMapping("/coordenadas")
    fun porCoordenadas(@RequestParam lat: Double, @RequestParam lon: Double): ResponseEntity<ClimaResponse> =
        ResponseEntity.ok(service.climaPorCoordenadas(lat, lon))

    // GET /api/clima/ciudades
    @GetMapping("/ciudades")
    fun ciudades(): ResponseEntity<List<CiudadResponse>> =
        ResponseEntity.ok(service.ciudades())

    // GET /api/clima/historial?ciudad=SANTIAGO  -> últimas 20 consultas guardadas
    @GetMapping("/historial")
    fun historial(@RequestParam(defaultValue = "VALPARAISO") ciudad: String): ResponseEntity<List<ConsultaClima>> =
        ResponseEntity.ok(service.historial(ciudad))
}
