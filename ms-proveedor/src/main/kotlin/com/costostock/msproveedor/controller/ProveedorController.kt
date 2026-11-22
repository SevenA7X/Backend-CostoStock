package com.costostock.msproveedor.controller

import com.costostock.msproveedor.dto.MetricasProveedorResponse
import com.costostock.msproveedor.dto.PublicacionResponse
import com.costostock.msproveedor.dto.PublicarLoteRequest
import com.costostock.msproveedor.model.Publicacion
import com.costostock.msproveedor.service.ProveedorService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/proveedor")
@CrossOrigin(origins = ["*"])
class ProveedorController(
    private val service: ProveedorService
) {
    // POST /api/proveedor/lotes  -> RF-05: formulario de publicación con foto nativa (Base64)
    @PostMapping("/lotes")
    fun publicar(@RequestBody request: PublicarLoteRequest): ResponseEntity<PublicacionResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.publicar(request))

    // GET /api/proveedor/fotos/{id}  -> devuelve la imagen (se puede abrir en el navegador)
    @GetMapping("/fotos/{id}")
    fun foto(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val foto = service.foto(id)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(foto.contentType))
            .body(foto.datos)
    }

    // GET /api/proveedor/{idProveedor}/publicaciones  -> historial de publicaciones del vendedor
    @GetMapping("/{idProveedor}/publicaciones")
    fun publicaciones(@PathVariable idProveedor: Long): ResponseEntity<List<Publicacion>> =
        ResponseEntity.ok(service.publicacionesDe(idProveedor))

    // GET /api/proveedor/{idProveedor}/metricas  -> RF-07: datos de ventas para la vista de métricas
    @GetMapping("/{idProveedor}/metricas")
    fun metricas(@PathVariable idProveedor: Long): ResponseEntity<MetricasProveedorResponse> =
        ResponseEntity.ok(service.metricas(idProveedor))
}
