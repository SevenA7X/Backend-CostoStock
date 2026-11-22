package cl.duoc.costostock.core.dto;

public record PublicarLoteRequest(
        Long idProveedor,
        String nombreComercio,
        String nombreProducto,
        String descripcion,
        String categoria,
        Integer stock,
        Integer montoLiquido,
        Integer precioNormal,
        String fechaCaducidad,
        Integer vigenciaHoras,
        String fotoBase64,
        Double latitud,
        Double longitud
) {
}