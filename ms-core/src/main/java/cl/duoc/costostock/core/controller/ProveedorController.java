package cl.duoc.costostock.core.controller;

import cl.duoc.costostock.core.dto.LoginProveedorRequest;
import cl.duoc.costostock.core.dto.LoginProveedorResponse;
import cl.duoc.costostock.core.dto.MetricasVendedorDto;
import cl.duoc.costostock.core.model.LoteCoreEntity;
import cl.duoc.costostock.core.model.Proveedor;
import cl.duoc.costostock.core.repository.LoteCoreRepository;
import cl.duoc.costostock.core.service.ProveedorService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    private final ProveedorService service;

    @Autowired(required = false)
    private LoteCoreRepository loteCoreRepository;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginProveedorResponse> login(@RequestBody LoginProveedorRequest req) {
        return ResponseEntity.ok(service.login(req));
    }

    @GetMapping("/acceso")
   public ResponseEntity acceso(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Proveedor p = service.validarAcceso(authorization);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("acceso", true);
        cuerpo.put("idProveedor", p.idProveedor());
        cuerpo.put("razonSocial", p.razonSocial());
        return ResponseEntity.ok(cuerpo);
    }

    @GetMapping("/{idProveedor}/metricas")
    public ResponseEntity<MetricasVendedorDto> obtenerMetricas(@PathVariable Long idProveedor) {
        List<LoteCoreEntity> lotes = (loteCoreRepository != null)
                ? loteCoreRepository.findByIdProveedor(idProveedor)
                : Collections.emptyList();

        int totalPublicados = lotes.size();
        int activos = (int) lotes.stream().filter(LoteCoreEntity::isActivo).count();
        int unidades = lotes.stream().mapToInt(l -> l.getStock() != null ? l.getStock() : 0).sum();
        int liquidoAcumulado = lotes.stream()
                .mapToInt(l -> (l.getMontoLiquido() != null ? l.getMontoLiquido() : 0)
                        * (l.getStock() != null ? l.getStock() : 1))
                .sum();
        int comisionAcumulada = lotes.stream()
                .mapToInt(l -> (l.getComision() != null ? l.getComision() : 0)
                        * (l.getStock() != null ? l.getStock() : 1))
                .sum();
        double kgCo2 = unidades * 1.5;
        int arboles = (int) Math.round(kgCo2 / 21.0);

        return ResponseEntity.ok(new MetricasVendedorDto(
                idProveedor,
                totalPublicados,
                activos,
                unidades,
                liquidoAcumulado + comisionAcumulada,
                liquidoAcumulado,
                comisionAcumulada,
                kgCo2,
                arboles
        ));
    }
}