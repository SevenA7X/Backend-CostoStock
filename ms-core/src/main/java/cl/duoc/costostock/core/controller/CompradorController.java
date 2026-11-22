package cl.duoc.costostock.core.controller;

import cl.duoc.costostock.core.dto.CompradorResponse;
import cl.duoc.costostock.core.dto.RegistroCompradorRequest;
import cl.duoc.costostock.core.model.Comprador;
import cl.duoc.costostock.core.service.CompradorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compradores")
public class CompradorController {

    private final CompradorService service;

    public CompradorController(CompradorService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    public ResponseEntity<CompradorResponse> registrar(@RequestBody RegistroCompradorRequest req) {
        CompradorService.Resultado r = service.registrarLigero(req);
        Comprador c = r.comprador();
        CompradorResponse cuerpo = new CompradorResponse(
                c.idComprador(), c.nombre(), c.correo(), c.telefono(), c.fechaRegistro(), r.esNuevo());
        return ResponseEntity.status(r.esNuevo() ? HttpStatus.CREATED : HttpStatus.OK).body(cuerpo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompradorResponse> obtener(@PathVariable("id") long id) {
        Comprador c = service.buscarPorId(id);
        return ResponseEntity.ok(new CompradorResponse(
                c.idComprador(), c.nombre(), c.correo(), c.telefono(), c.fechaRegistro(), false));
    }
}
