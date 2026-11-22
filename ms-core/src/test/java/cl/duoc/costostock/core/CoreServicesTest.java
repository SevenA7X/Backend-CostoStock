package cl.duoc.costostock.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.costostock.core.dto.LoginProveedorRequest;
import cl.duoc.costostock.core.dto.LoginProveedorResponse;
import cl.duoc.costostock.core.dto.RegistroCompradorRequest;
import cl.duoc.costostock.core.exception.ApiException;
import cl.duoc.costostock.core.service.CompradorService;
import cl.duoc.costostock.core.service.ProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CoreServicesTest {

    private final CompradorService compradores = new CompradorService();
    private final ProveedorService proveedores = new ProveedorService();

    @Test
    void registroLigero_creaCompradorNuevo() {
        CompradorService.Resultado r = compradores.registrarLigero(
                new RegistroCompradorRequest("Romina Carrasco", "romina@correo.cl", "912345678"));
        assertTrue(r.esNuevo());
        assertEquals(1L, r.comprador().idComprador());
        assertEquals("romina@correo.cl", r.comprador().correo());
    }

    @Test
    void registroLigero_mismoCorreo_reutilizaId() {
        compradores.registrarLigero(new RegistroCompradorRequest("Ana", "ana@correo.cl", "911111111"));
        CompradorService.Resultado r = compradores.registrarLigero(
                new RegistroCompradorRequest("Ana Pérez", "ANA@correo.cl", "922222222"));
        assertFalse(r.esNuevo());
        assertEquals(1L, r.comprador().idComprador());
        assertEquals("Ana Pérez", r.comprador().nombre());
    }

    @Test
    void registroLigero_datosInvalidos_lanza400() {
        ApiException e = assertThrows(ApiException.class, () -> compradores.registrarLigero(
                new RegistroCompradorRequest("", "abc", "123")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertTrue(e.getMessage().contains("nombre"));
        assertTrue(e.getMessage().contains("correo"));
        assertTrue(e.getMessage().contains("teléfono"));
    }

    @Test
    void loginProveedor_correcto_devuelveToken() {
        LoginProveedorResponse r = proveedores.login(new LoginProveedorRequest("bodega@costostock.cl", "Bodega2026!"));
        assertNotNull(r.token());
        assertEquals(1L, r.idProveedor());
        assertEquals(1L, proveedores.validarAcceso("Bearer " + r.token()).idProveedor());
    }

    @Test
    void loginProveedor_contrasenaIncorrecta_lanza401() {
        ApiException e = assertThrows(ApiException.class,
                () -> proveedores.login(new LoginProveedorRequest("bodega@costostock.cl", "mala")));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void acceso_sinToken_lanza401() {
        ApiException e = assertThrows(ApiException.class, () -> proveedores.validarAcceso(null));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }
}
