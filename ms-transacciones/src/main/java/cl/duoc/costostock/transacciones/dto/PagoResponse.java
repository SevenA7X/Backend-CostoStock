package cl.duoc.costostock.transacciones.dto;

public record PagoResponse(
        String idTransaccion,
        String idReserva,
        String estado,
        String fechaPago,
        String codigoVoucher,
        String nombreProducto,
        String tiendaNombre,
        String direccionRetiro,
        String horarioRetiro,
        int cantidad,
        int subtotal,
        int comisionServicio,
        int montoTotal,
        double kgCo2Evitado,
        int stockRestante) {
}
