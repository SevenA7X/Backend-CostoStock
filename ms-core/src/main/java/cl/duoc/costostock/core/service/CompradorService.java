package cl.duoc.costostock.core.service;

import cl.duoc.costostock.core.dto.RegistroCompradorRequest;
import cl.duoc.costostock.core.exception.ApiException;
import cl.duoc.costostock.core.model.Comprador;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class CompradorService {

    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern SOLO_DIGITOS = Pattern.compile("^[0-9]+$");

    private final Map<String, Comprador> porCorreo = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public record Resultado(Comprador comprador, boolean esNuevo) {
    }

    public Resultado registrarLigero(RegistroCompradorRequest req) {
        List<String> errores = validar(req);
        if (!errores.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", String.join(" ", errores));
        }

        String nombre = req.nombre().trim();
        String correo = req.correo().trim().toLowerCase();
        String telefono = req.telefono().trim();
        boolean[] esNuevo = {false};

        Comprador comprador = porCorreo.compute(correo, (clave, existente) -> {
            if (existente == null) {
                esNuevo[0] = true;
                return new Comprador(secuencia.incrementAndGet(), nombre, correo, telefono, ahora());
            }
            
            return new Comprador(existente.idComprador(), nombre, correo, telefono, existente.fechaRegistro());
        });
        return new Resultado(comprador, esNuevo[0]);
    }

    public Comprador buscarPorId(long id) {
        return porCorreo.values().stream()
                .filter(c -> c.idComprador() == id)
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMPRADOR_NO_ENCONTRADO",
                        "No existe un comprador con id " + id));
    }

    List<String> validar(RegistroCompradorRequest req) {
        List<String> errores = new ArrayList<>();
        if (req == null) {
            errores.add("El cuerpo de la solicitud es obligatorio.");
            return errores;
        }
        if (req.nombre() == null || req.nombre().trim().isEmpty()) {
            errores.add("El nombre es obligatorio.");
        }
        if (req.correo() == null || req.correo().trim().isEmpty()) {
            errores.add("El correo es obligatorio.");
        } else if (!CORREO.matcher(req.correo().trim()).matches()) {
            errores.add("Formato de correo inválido.");
        }
        if (req.telefono() == null || req.telefono().trim().isEmpty()) {
            errores.add("El teléfono es obligatorio.");
        } else if (!SOLO_DIGITOS.matcher(req.telefono().trim()).matches()) {
            errores.add("El teléfono debe contener solo dígitos numéricos.");
        } else if (req.telefono().trim().length() < 8) {
            errores.add("El teléfono debe tener al menos 8 dígitos.");
        }
        return errores;
    }

    private String ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
