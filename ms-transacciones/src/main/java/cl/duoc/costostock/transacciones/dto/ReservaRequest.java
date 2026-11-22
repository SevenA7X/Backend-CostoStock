package cl.duoc.costostock.transacciones.dto;


public record ReservaRequest(Long idLote, Integer cantidad, Long idComprador, Boolean esInvitado) {
}
