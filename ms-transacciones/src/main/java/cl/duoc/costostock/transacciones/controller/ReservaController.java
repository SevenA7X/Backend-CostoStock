package cl.duoc.costostock.transacciones.controller;

import cl.duoc.costostock.transacciones.dto.ReservaRequest;
import cl.duoc.costostock.transacciones.dto.ReservaResponse;
import cl.duoc.costostock.transacciones.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@RequestBody ReservaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtener(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.obtener(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
