package cl.duoc.costostock.core.controller;

import cl.duoc.costostock.core.dto.LotePublicadoDto;
import cl.duoc.costostock.core.dto.PublicarLoteRequest;
import cl.duoc.costostock.core.model.LoteCoreEntity;
import cl.duoc.costostock.core.repository.LoteCoreRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = "*")
public class LoteController {

    private final LoteCoreRepository loteRepository;

    public LoteController(LoteCoreRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    @PostMapping
    public ResponseEntity<LotePublicadoDto> publicarLote(@RequestBody PublicarLoteRequest req) {
        int liquido = req.montoLiquido() != null ? req.montoLiquido() : 0;
        int comision = (int) Math.round(liquido * 0.10);
        int precioFinal = liquido + comision;

        LoteCoreEntity entity = new LoteCoreEntity();
        entity.setIdProveedor(req.idProveedor() != null ? req.idProveedor() : 1L);
        entity.setNombreComercio(req.nombreComercio());
        entity.setNombreProducto(req.nombreProducto());
        entity.setDescripcion(req.descripcion());
        entity.setCategoria(req.categoria());
        entity.setStock(req.stock() != null ? req.stock() : 1);
        entity.setMontoLiquido(liquido);
        entity.setComision(comision);
        entity.setPrecioOfertaFinal(precioFinal);
        entity.setPrecioNormal(req.precioNormal());
        entity.setFechaCaducidad(req.fechaCaducidad());
        entity.setVigenciaHoras(req.vigenciaHoras());
        entity.setFotoBase64(req.fotoBase64());
        entity.setLatitud(req.latitud());
        entity.setLongitud(req.longitud());

        LoteCoreEntity guardado = loteRepository.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(new LotePublicadoDto(
                guardado.getId(),
                guardado.getNombreProducto(),
                guardado.getMontoLiquido(),
                guardado.getComision(),
                guardado.getPrecioOfertaFinal()
        ));
    }

    @GetMapping
    public List<LoteCoreEntity> listarLotes() {
        return loteRepository.findAll();
    }
}