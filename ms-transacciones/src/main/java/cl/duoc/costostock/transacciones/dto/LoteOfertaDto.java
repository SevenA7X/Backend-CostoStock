package cl.duoc.costostock.transacciones.dto;

import cl.duoc.costostock.transacciones.model.Lote;
import java.time.temporal.ChronoUnit;

public record LoteOfertaDto(
        Long id,
        String nombre,
        String descripcion,
        String categoria,
        double precioNormal,
        double precioOferta,
        int stock,
        double latitud,
        double longitud,
        String finOferta,
        String fechaCaducidad,
        double kgCo2Evitado,
        boolean esAlcohol,
        String tiendaNombre,
        boolean activo) {

    public static LoteOfertaDto desde(Lote l) {
        return new LoteOfertaDto(
                l.getId(), l.getNombre(), l.getDescripcion(), l.getCategoria(),
                l.getPrecioNormal(), l.getPrecioOferta(), l.getStock(),
                l.getLatitud(), l.getLongitud(),
                l.getFinOferta().truncatedTo(ChronoUnit.SECONDS).toString(),
                l.getFechaCaducidad().truncatedTo(ChronoUnit.SECONDS).toString(),
                l.getKgCo2Evitado(), l.isEsAlcohol(), l.getTiendaNombre(), l.isActivo());
    }
}
