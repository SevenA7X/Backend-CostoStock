package cl.duoc.costostock.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "lotes_core")
public class LoteCoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idProveedor;
    private String nombreComercio;
    private String nombreProducto;

    @Column(length = 1000)
    private String descripcion;

    private String categoria;
    private Integer stock;
    private Integer montoLiquido;
    private Integer comision;
    private Integer precioOfertaFinal;
    private Integer precioNormal;
    private String fechaCaducidad;
    private Integer vigenciaHoras;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String fotoBase64;

    private Double latitud;
    private Double longitud;
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }

    public String getNombreComercio() { return nombreComercio; }
    public void setNombreComercio(String nombreComercio) { this.nombreComercio = nombreComercio; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getMontoLiquido() { return montoLiquido; }
    public void setMontoLiquido(Integer montoLiquido) { this.montoLiquido = montoLiquido; }

    public Integer getComision() { return comision; }
    public void setComision(Integer comision) { this.comision = comision; }

    public Integer getPrecioOfertaFinal() { return precioOfertaFinal; }
    public void setPrecioOfertaFinal(Integer precioOfertaFinal) { this.precioOfertaFinal = precioOfertaFinal; }

    public Integer getPrecioNormal() { return precioNormal; }
    public void setPrecioNormal(Integer precioNormal) { this.precioNormal = precioNormal; }

    public String getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(String fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public Integer getVigenciaHoras() { return vigenciaHoras; }
    public void setVigenciaHoras(Integer vigenciaHoras) { this.vigenciaHoras = vigenciaHoras; }

    public String getFotoBase64() { return fotoBase64; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}