package cl.duoc.costostock.transacciones.service;

import cl.duoc.costostock.transacciones.dto.ReservaRequest;
import cl.duoc.costostock.transacciones.dto.ReservaResponse;
import cl.duoc.costostock.transacciones.exception.ApiException;
import cl.duoc.costostock.transacciones.model.EstadoReserva;
import cl.duoc.costostock.transacciones.model.Lote;
import cl.duoc.costostock.transacciones.model.Reserva;
import cl.duoc.costostock.transacciones.repository.LoteRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class ReservaService {

    public static final int MINUTOS_RESERVA = 10;
    public static final int CANTIDAD_MAXIMA = 10;
    private static final double PORCENTAJE_COMISION = 0.05;

    private final LoteRepository lotes;
    private final Map<String, Reserva> reservas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1000);
    private Supplier<LocalDateTime> reloj = LocalDateTime::now;

    public ReservaService(LoteRepository lotes) {
        this.lotes = lotes;
    }


    public void usarReloj(Supplier<LocalDateTime> nuevoReloj) {
        this.reloj = nuevoReloj;
    }

    public LocalDateTime ahora() {
        return reloj.get();
    }

    public ReservaResponse crear(ReservaRequest req) {
        if (req == null || req.idLote() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El idLote es obligatorio.");
        }
        int cantidad = req.cantidad() == null ? 1 : req.cantidad();
        if (cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS",
                    "La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA + ".");
        }

        Lote lote = lotes.buscar(req.idLote()).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "LOTE_NO_ENCONTRADO", "No existe el lote " + req.idLote() + "."));

        LocalDateTime ahora = ahora();
        if (!lote.isActivo() || !ahora.isBefore(lote.getFechaCaducidad())) {
            throw new ApiException(HttpStatus.CONFLICT, "LOTE_NO_DISPONIBLE", "El lote ya no está disponible.");
        }


        if (!lote.tomarStock(cantidad)) {
            throw new ApiException(HttpStatus.CONFLICT, "STOCK_INSUFICIENTE",
                    "Stock insuficiente: quedan " + lote.getStock() + " unidades.");
        }

        int subtotal = lote.precioVigente(ahora) * cantidad;
        int comision = (int) (subtotal * PORCENTAJE_COMISION);
        int total = subtotal + comision;
        double kgCo2 = Math.round(lote.getKgCo2Evitado() * cantidad * 1000.0) / 1000.0;
        boolean invitado = req.esInvitado() != null ? req.esInvitado() : req.idComprador() == null;

        String id = "RES-" + ahora.getYear() + "-" + secuencia.incrementAndGet();
        Reserva reserva = new Reserva(id, lote.getId(), cantidad, subtotal, comision, total, kgCo2,
                ahora, ahora.plusMinutes(MINUTOS_RESERVA), invitado, req.idComprador());
        reservas.put(id, reserva);
        return ReservaResponse.desde(reserva, lote, ahora);
    }

    public ReservaResponse obtener(String idReserva) {
        Reserva reserva = entidad(idReserva);
        expirarSiCorresponde(reserva);
        return ReservaResponse.desde(reserva, loteDe(reserva), ahora());
    }


    public ReservaResponse cancelar(String idReserva) {
        Reserva reserva = entidad(idReserva);
        synchronized (reserva) {
            expirarSiCorresponde(reserva);
            if (reserva.getEstado() == EstadoReserva.PAGADA) {
                throw new ApiException(HttpStatus.CONFLICT, "RESERVA_NO_CANCELABLE", "La reserva ya fue pagada.");
            }
            if (reserva.getEstado() == EstadoReserva.ACTIVA) {
                reserva.setEstado(EstadoReserva.CANCELADA);
                loteDe(reserva).devolverStock(reserva.getCantidad());
            }
        }
        return ReservaResponse.desde(reserva, loteDe(reserva), ahora());
    }

    public Reserva entidad(String idReserva) {
        Reserva reserva = idReserva == null ? null : reservas.get(idReserva);
        if (reserva == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "RESERVA_NO_ENCONTRADA", "No existe la reserva " + idReserva + ".");
        }
        return reserva;
    }


    public boolean expirarSiCorresponde(Reserva reserva) {
        synchronized (reserva) {
            if (reserva.getEstado() == EstadoReserva.ACTIVA && !ahora().isBefore(reserva.getExpiraEn())) {
                reserva.setEstado(EstadoReserva.EXPIRADA);
                loteDe(reserva).devolverStock(reserva.getCantidad());
                return true;
            }
            return false;
        }
    }

    public int expirarVencidas() {
        int expiradas = 0;
        for (Reserva reserva : reservas.values()) {
            if (expirarSiCorresponde(reserva)) {
                expiradas++;
            }
        }
        return expiradas;
    }

    public Lote loteDe(Reserva reserva) {
        return lotes.buscar(reserva.getIdLote()).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "LOTE_NO_ENCONTRADO", "No existe el lote " + reserva.getIdLote() + "."));
    }
}
