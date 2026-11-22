package cl.duoc.costostock.transacciones.model;

import java.time.LocalDateTime;

/** Reserva temporal de stock (RF-12). Se modifica siempre dentro de un bloque synchronized(reserva). */
public class Reserva {

    private final String idReserva;
    private final long idLote;
    private final int cantidad;
    private final int subtotal;
    private final int comisionServicio;
    private final int total;
    private final double kgCo2Evitado;
    private final LocalDateTime creadaEn;
    private final LocalDateTime expiraEn;
    private final boolean esInvitado;
    private EstadoReserva estado = EstadoReserva.ACTIVA;
    private Long idComprador;
    private String idTransaccion;
    private LocalDateTime fechaPago;

    public Reserva(String idReserva, long idLote, int cantidad, int subtotal, int comisionServicio, int total,
                   double kgCo2Evitado, LocalDateTime creadaEn, LocalDateTime expiraEn, boolean esInvitado,
                   Long idComprador) {
        this.idReserva = idReserva;
        this.idLote = idLote;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.comisionServicio = comisionServicio;
        this.total = total;
        this.kgCo2Evitado = kgCo2Evitado;
        this.creadaEn = creadaEn;
        this.expiraEn = expiraEn;
        this.esInvitado = esInvitado;
        this.idComprador = idComprador;
    }

    public String getIdReserva() { return idReserva; }
    public long getIdLote() { return idLote; }
    public int getCantidad() { return cantidad; }
    public int getSubtotal() { return subtotal; }
    public int getComisionServicio() { return comisionServicio; }
    public int getTotal() { return total; }
    public double getKgCo2Evitado() { return kgCo2Evitado; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public boolean isEsInvitado() { return esInvitado; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public Long getIdComprador() { return idComprador; }
    public void setIdComprador(Long idComprador) { this.idComprador = idComprador; }
    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}
