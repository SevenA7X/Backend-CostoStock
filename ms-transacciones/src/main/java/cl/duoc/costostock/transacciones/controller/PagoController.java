package cl.duoc.costostock.transacciones.controller;

import cl.duoc.costostock.transacciones.dto.PagoRequest;
import cl.duoc.costostock.transacciones.dto.PagoResponse;
import cl.duoc.costostock.transacciones.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<PagoResponse> pagar(@RequestBody PagoRequest req) {
        return ResponseEntity.ok(service.pagar(req));
    }
}
