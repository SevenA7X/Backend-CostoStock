package cl.duoc.costostock.transacciones.dto;


public record PagoRequest(String idReserva, Long idComprador, String metodoPago, Boolean simularFallo) {
}
