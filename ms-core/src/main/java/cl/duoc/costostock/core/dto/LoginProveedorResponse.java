package cl.duoc.costostock.core.dto;

public record LoginProveedorResponse(
        long idProveedor,
        String razonSocial,
        String token,
        String expiraEn) {
}
