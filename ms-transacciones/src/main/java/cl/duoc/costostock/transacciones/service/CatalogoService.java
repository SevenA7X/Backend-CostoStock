package cl.duoc.costostock.transacciones.service;

import cl.duoc.costostock.transacciones.dto.LoteOfertaDto;
import cl.duoc.costostock.transacciones.model.Lote;
import cl.duoc.costostock.transacciones.repository.LoteRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CatalogoService {

    private static final double RADIO_TIERRA_KM = 6371.0;
    private static final double RADIO_POR_DEFECTO_KM = 30.0;

    private final LoteRepository repositorio;

    public CatalogoService(LoteRepository repositorio) {
        this.repositorio = repositorio;
    }


    public List<LoteOfertaDto> listar(Double lat, Double lon, Double radioKm) {
        LocalDateTime ahora = LocalDateTime.now();
        double radio = (radioKm == null || radioKm <= 0) ? RADIO_POR_DEFECTO_KM : radioKm;
        boolean filtrarPorDistancia = lat != null && lon != null;

        return repositorio.todos().stream()
                .filter(l -> l.isActivo() && l.getStock() > 0 && ahora.isBefore(l.getFechaCaducidad()))
                .filter(l -> !filtrarPorDistancia
                        || distanciaKm(lat, lon, l.getLatitud(), l.getLongitud()) <= radio)
                .map(LoteOfertaDto::desde)
                .collect(Collectors.toList());
    }


    public static double distanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}
