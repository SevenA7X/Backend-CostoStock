package cl.duoc.costostock.transacciones.dto;

import cl.duoc.costostock.transacciones.model.Lote;
import cl.duoc.costostock.transacciones.model.Reserva;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ReservaResponse(
        String idReserva,
        long idLote,
        String nombreProducto,
        String tiendaNombre,
        int cantidad,
        int subtotal,
        int comisionServicio,
        int total,
        double kgCo2Evitado,
        String estado,
        String creadaEn,
        String expiraEn,
        long segundosRestantes,
        boolean esInvitado) {

    public static ReservaResponse desde(Reserva r, Lote lote, LocalDateTime ahora) {
        long restantes = Math.max(0, Duration.between(ahora, r.getExpiraEn()).getSeconds());
        return new ReservaResponse(
                r.getIdReserva(), r.getIdLote(), lote.getNombre(), lote.getTiendaNombre(),
                r.getCantidad(), r.getSubtotal(), r.getComisionServicio(), r.getTotal(),
                r.getKgCo2Evitado(), r.getEstado().name(),
                r.getCreadaEn().truncatedTo(ChronoUnit.SECONDS).toString(),
                r.getExpiraEn().truncatedTo(ChronoUnit.SECONDS).toString(),
                restantes, r.isEsInvitado());
    }
}
