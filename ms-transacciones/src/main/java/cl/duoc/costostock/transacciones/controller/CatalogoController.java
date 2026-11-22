package cl.duoc.costostock.transacciones.controller;

import cl.duoc.costostock.transacciones.dto.LoteOfertaDto;
import cl.duoc.costostock.transacciones.service.CatalogoService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    /** GET /api/catalogo?lat=..&lon=..&radioKm=.. (lo consume CatalogoViewModel de la app). */
    @GetMapping
    public ResponseEntity<List<LoteOfertaDto>> catalogo(
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon,
            @RequestParam(value = "radioKm", required = false) Double radioKm) {
        return ResponseEntity.ok(service.listar(lat, lon, radioKm));
    }
}
