package cl.duoc.costostock.transacciones.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("servicio", "ms-transacciones");
        cuerpo.put("estado", "UP");
        cuerpo.put("puerto", 8081);
        return ResponseEntity.ok(cuerpo);
    }
}
