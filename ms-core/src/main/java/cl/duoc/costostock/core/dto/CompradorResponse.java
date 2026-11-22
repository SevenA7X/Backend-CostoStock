package cl.duoc.costostock.core.dto;

public record CompradorResponse(
        long idComprador,
        String nombre,
        String correo,
        String telefono,
        String fechaRegistro,
        boolean esNuevo) {
}
