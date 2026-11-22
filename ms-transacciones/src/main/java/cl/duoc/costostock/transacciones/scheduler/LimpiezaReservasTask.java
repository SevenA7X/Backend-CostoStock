package cl.duoc.costostock.transacciones.scheduler;

import cl.duoc.costostock.transacciones.service.ReservaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LimpiezaReservasTask {

    private final ReservaService reservas;

    public LimpiezaReservasTask(ReservaService reservas) {
        this.reservas = reservas;
    }

    @Scheduled(fixedRate = 15000)
    public void liberarReservasVencidas() {
        reservas.expirarVencidas();
    }
}
