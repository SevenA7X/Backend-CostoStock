package cl.duoc.costostock.core.service;

import cl.duoc.costostock.core.dto.LoginProveedorRequest;
import cl.duoc.costostock.core.dto.LoginProveedorResponse;
import cl.duoc.costostock.core.exception.ApiException;
import cl.duoc.costostock.core.model.Proveedor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class ProveedorService {

    private static final int HORAS_SESION = 8;

    private record Sesion(long idProveedor, LocalDateTime expiraEn) {
    }

    private final Map<String, Proveedor> porCorreo = new ConcurrentHashMap<>();
    private final Map<Long, Proveedor> porId = new ConcurrentHashMap<>();
    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

    public ProveedorService() {
        
        registrar(1, "Bodega Viña Centro", "bodega@costostock.cl", "Bodega2026!");
        registrar(2, "Licorería El Puerto", "licoreria@costostock.cl", "Puerto2026!");
        registrar(3, "Farmacia San Juan", "farmacia@costostock.cl", "Farmacia2026!");
    }

    private void registrar(long id, String razonSocial, String correo, String contrasena) {
        Proveedor p = new Proveedor(id, razonSocial, correo, hash(contrasena));
        porCorreo.put(correo, p);
        porId.put(id, p);
    }

    public LoginProveedorResponse login(LoginProveedorRequest req) {
        if (req == null || req.correo() == null || req.correo().isBlank()
                || req.contrasena() == null || req.contrasena().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "Correo y contraseña son obligatorios.");
        }
        Proveedor p = porCorreo.get(req.correo().trim().toLowerCase());
        boolean ok = p != null && MessageDigest.isEqual(
                p.passwordHash().getBytes(StandardCharsets.UTF_8),
                hash(req.contrasena()).getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos.");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expira = LocalDateTime.now().plusHours(HORAS_SESION).truncatedTo(ChronoUnit.SECONDS);
        sesiones.put(token, new Sesion(p.idProveedor(), expira));
        return new LoginProveedorResponse(p.idProveedor(), p.razonSocial(), token, expira.toString());
    }

    
    public Proveedor validarAcceso(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_AUSENTE", "Falta el encabezado Authorization: Bearer <token>.");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "El token no es válido.");
        }
        if (LocalDateTime.now().isAfter(sesion.expiraEn())) {
            sesiones.remove(token);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRADO", "La sesión expiró, inicia sesión nuevamente.");
        }
        return porId.get(sesion.idProveedor());
    }

    static String hash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
