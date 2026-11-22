package cl.duoc.costostock.core.dto;

public record LotePublicadoDto(
        Long id,
        String nombreProducto,
        Integer montoLiquido,
        Integer comision,
        Integer precioOfertaFinal
) {
}