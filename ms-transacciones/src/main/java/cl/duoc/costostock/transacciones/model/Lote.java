package cl.duoc.costostock.transacciones.model;

import java.time.LocalDateTime;

/** Lote de productos en oferta. El stock se modifica solo con los métodos sincronizados. */
public class Lote {

    private final long id;
    private final String nombre;
    private final String descripcion;
    private final String categoria;
    private final double precioNormal;
    private final double precioOferta;
    private int stock;
    private final double latitud;
    private final double longitud;
    private final LocalDateTime finOferta;
    private final LocalDateTime fechaCaducidad;
    private final double kgCo2Evitado;
    private final boolean esAlcohol;
    private final String tiendaNombre;
    private final String direccionRetiro;
    private final String horarioRetiro;
    private boolean activo = true;

    public Lote(long id, String nombre, String descripcion, String categoria,
                double precioNormal, double precioOferta, int stock,
                double latitud, double longitud,
                LocalDateTime finOferta, LocalDateTime fechaCaducidad,
                double kgCo2Evitado, boolean esAlcohol,
                String tiendaNombre, String direccionRetiro, String horarioRetiro) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precioNormal = precioNormal;
        this.precioOferta = precioOferta;
        this.stock = stock;
        this.latitud = latitud;
        this.longitud = longitud;
        this.finOferta = finOferta;
        this.fechaCaducidad = fechaCaducidad;
        this.kgCo2Evitado = kgCo2Evitado;
        this.esAlcohol = esAlcohol;
        this.tiendaNombre = tiendaNombre;
        this.direccionRetiro = direccionRetiro;
        this.horarioRetiro = horarioRetiro;
    }

    /** Descuenta stock de forma atómica. Devuelve false si no alcanza. */
    public synchronized boolean tomarStock(int cantidad) {
        if (cantidad <= 0 || stock < cantidad) {
            return false;
        }
        stock -= cantidad;
        return true;
    }

    /** Devuelve stock (reserva expirada o cancelada). */
    public synchronized void devolverStock(int cantidad) {
        if (cantidad > 0) {
            stock += cantidad;
        }
    }

    public synchronized int getStock() {
        return stock;
    }

    /** Precio unitario vigente: el de oferta mientras dure la oferta; después, el normal. */
    public int precioVigente(LocalDateTime ahora) {
        double precio = ahora.isBefore(finOferta) ? precioOferta : precioNormal;
        return (int) Math.round(precio);
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public double getPrecioNormal() { return precioNormal; }
    public double getPrecioOferta() { return precioOferta; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public LocalDateTime getFinOferta() { return finOferta; }
    public LocalDateTime getFechaCaducidad() { return fechaCaducidad; }
    public double getKgCo2Evitado() { return kgCo2Evitado; }
    public boolean isEsAlcohol() { return esAlcohol; }
    public String getTiendaNombre() { return tiendaNombre; }
    public String getDireccionRetiro() { return direccionRetiro; }
    public String getHorarioRetiro() { return horarioRetiro; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
