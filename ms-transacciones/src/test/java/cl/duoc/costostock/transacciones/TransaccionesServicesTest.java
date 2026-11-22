package cl.duoc.costostock.transacciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.costostock.transacciones.dto.LoteOfertaDto;
import cl.duoc.costostock.transacciones.dto.PagoRequest;
import cl.duoc.costostock.transacciones.dto.PagoResponse;
import cl.duoc.costostock.transacciones.dto.ReservaRequest;
import cl.duoc.costostock.transacciones.dto.ReservaResponse;
import cl.duoc.costostock.transacciones.exception.ApiException;
import cl.duoc.costostock.transacciones.repository.LoteRepository;
import cl.duoc.costostock.transacciones.service.CatalogoService;
import cl.duoc.costostock.transacciones.service.PagoService;
import cl.duoc.costostock.transacciones.service.ReservaService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class TransaccionesServicesTest {

    private final LoteRepository repositorio = new LoteRepository();
    private final ReservaService reservas = new ReservaService(repositorio);
    private final PagoService pagos = new PagoService(reservas);
    private final CatalogoService catalogo = new CatalogoService(repositorio);
    private final AtomicReference<LocalDateTime> reloj = new AtomicReference<>(LocalDateTime.now());

    TransaccionesServicesTest() {
        reservas.usarReloj(reloj::get);
    }

    private int stock(long idLote) {
        return repositorio.buscar(idLote).orElseThrow().getStock();
    }

    @Test
    void reservar_descuentaStockYFijaExpiracionDe10Minutos() {
        ReservaResponse r = reservas.crear(new ReservaRequest(1L, 2, null, true));
        assertEquals(8, stock(1L));
        assertEquals("ACTIVA", r.estado());
        assertEquals(600L, r.segundosRestantes());
        assertEquals(8800, r.subtotal());          // 2 x 4.400
        assertEquals(440, r.comisionServicio());   // 5% de 8.800
        assertEquals(9240, r.total());
        assertTrue(r.esInvitado());
    }

    @Test
    void reservar_sinStockSuficiente_lanza409() {
        ApiException e = assertThrows(ApiException.class, () -> reservas.crear(new ReservaRequest(8L, 3, null, true)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("STOCK_INSUFICIENTE", e.getCodigo());
        assertEquals(2, stock(8L));
    }

    @Test
    void reservar_loteInexistente_lanza404() {
        ApiException e = assertThrows(ApiException.class, () -> reservas.crear(new ReservaRequest(999L, 1, null, true)));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void reservaVencida_devuelveElStock() {
        reservas.crear(new ReservaRequest(1L, 3, null, true));
        assertEquals(7, stock(1L));

        reloj.set(reloj.get().plusMinutes(11));
        assertEquals(1, reservas.expirarVencidas());
        assertEquals(10, stock(1L));
    }

    @Test
    void cancelar_devuelveElStock() {
        ReservaResponse r = reservas.crear(new ReservaRequest(1L, 1, null, true));
        assertEquals(9, stock(1L));
        assertEquals("CANCELADA", reservas.cancelar(r.idReserva()).estado());
        assertEquals(10, stock(1L));
    }

    @Test
    void pagar_confirmaLaVentaYMantieneElStockDescontado() {
        ReservaResponse r = reservas.crear(new ReservaRequest(1L, 1, null, true));
        PagoResponse p = pagos.pagar(new PagoRequest(r.idReserva(), 7L, "TARJETA_SIMULADA", false));
        assertEquals("APROBADO", p.estado());
        assertEquals(r.total(), p.montoTotal());
        assertTrue(p.codigoVoucher().startsWith("QR-"));
        assertEquals(9, p.stockRestante());
        assertEquals(9, stock(1L));
    }

    @Test
    void pagar_dosVeces_lanza409() {
        ReservaResponse r = reservas.crear(new ReservaRequest(1L, 1, null, true));
        pagos.pagar(new PagoRequest(r.idReserva(), null, null, false));
        ApiException e = assertThrows(ApiException.class,
                () -> pagos.pagar(new PagoRequest(r.idReserva(), null, null, false)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void pagar_reservaExpirada_lanza410() {
        ReservaResponse r = reservas.crear(new ReservaRequest(1L, 1, null, true));
        reloj.set(reloj.get().plusMinutes(10).plusSeconds(1));
        ApiException e = assertThrows(ApiException.class,
                () -> pagos.pagar(new PagoRequest(r.idReserva(), null, null, false)));
        assertEquals(HttpStatus.GONE, e.getStatus());
        assertEquals(10, stock(1L));
    }

    @Test
    void pagar_conFalloSimulado_lanza402YLaReservaSigueActiva() {
        ReservaResponse r = reservas.crear(new ReservaRequest(1L, 1, null, true));
        ApiException e = assertThrows(ApiException.class,
                () -> pagos.pagar(new PagoRequest(r.idReserva(), null, null, true)));
        assertEquals(HttpStatus.PAYMENT_REQUIRED, e.getStatus());
        assertEquals("ACTIVA", reservas.obtener(r.idReserva()).estado());
        assertEquals("APROBADO", pagos.pagar(new PagoRequest(r.idReserva(), null, null, false)).estado());
    }

    @Test
    void catalogo_filtraPorRadio() {
        List<LoteOfertaDto> cerca = catalogo.listar(-33.0245, -71.5518, 5.0);
        assertTrue(cerca.stream().anyMatch(l -> l.id() == 1L));
        assertFalse(cerca.stream().anyMatch(l -> l.id() == 2L));
        assertEquals(12, catalogo.listar(-33.0245, -71.5518, 30.0).size());
        assertEquals(12, catalogo.listar(null, null, null).size());
    }

    @Test
    void catalogo_ocultaLotesSinStock() {
        reservas.crear(new ReservaRequest(8L, 2, null, true));
        assertFalse(catalogo.listar(null, null, null).stream().anyMatch(l -> l.id() == 8L));
    }
}
