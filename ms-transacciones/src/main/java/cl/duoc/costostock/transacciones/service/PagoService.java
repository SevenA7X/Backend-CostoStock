package cl.duoc.costostock.transacciones.service;

import cl.duoc.costostock.transacciones.dto.PagoRequest;
import cl.duoc.costostock.transacciones.dto.PagoResponse;
import cl.duoc.costostock.transacciones.exception.ApiException;
import cl.duoc.costostock.transacciones.model.EstadoReserva;
import cl.duoc.costostock.transacciones.model.Lote;
import cl.duoc.costostock.transacciones.model.Reserva;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class PagoService {

    private final ReservaService reservas;

    public PagoService(ReservaService reservas) {
        this.reservas = reservas;
    }

    public PagoResponse pagar(PagoRequest req) {
        if (req == null || req.idReserva() == null || req.idReserva().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El idReserva es obligatorio.");
        }
        Reserva reserva = reservas.entidad(req.idReserva().trim());
        String idTransaccion;

        synchronized (reserva) {
            reservas.expirarSiCorresponde(reserva);

            switch (reserva.getEstado()) {
                case PAGADA -> throw new ApiException(HttpStatus.CONFLICT, "RESERVA_YA_PAGADA", "La reserva ya fue pagada.");
                case EXPIRADA -> throw new ApiException(HttpStatus.GONE, "RESERVA_EXPIRADA",
                        "La reserva expiró (10 minutos) y el stock fue liberado.");
                case CANCELADA -> throw new ApiException(HttpStatus.GONE, "RESERVA_CANCELADA", "La reserva fue cancelada.");
                default -> {
                    // ACTIVA: se puede pagar
                }
            }

            if (Boolean.TRUE.equals(req.simularFallo())) {
                throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "PAGO_RECHAZADO",
                        "La pasarela simulada rechazó el pago. Intenta nuevamente.");
            }

            idTransaccion = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            reserva.setEstado(EstadoReserva.PAGADA);
            reserva.setIdTransaccion(idTransaccion);
            reserva.setFechaPago(reservas.ahora());
            if (req.idComprador() != null) {
                reserva.setIdComprador(req.idComprador());
            }
        }

        Lote lote = reservas.loteDe(reserva);
        String codigoVoucher = "QR-" + reserva.getIdReserva().replace("RES-", "") + "-" + idTransaccion.substring(3);
        return new PagoResponse(
                idTransaccion,
                reserva.getIdReserva(),
                "APROBADO",
                reserva.getFechaPago().truncatedTo(ChronoUnit.SECONDS).toString(),
                codigoVoucher,
                lote.getNombre(),
                lote.getTiendaNombre(),
                lote.getDireccionRetiro(),
                lote.getHorarioRetiro(),
                reserva.getCantidad(),
                reserva.getSubtotal(),
                reserva.getComisionServicio(),
                reserva.getTotal(),
                reserva.getKgCo2Evitado(),
                lote.getStock());
    }
}
