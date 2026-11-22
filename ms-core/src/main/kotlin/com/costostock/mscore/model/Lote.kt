package com.costostock.mscore.model

import jakarta.persistence.*
import java.time.LocalDateTime

// Lote publicado por un proveedor (MS-Core = gestión de ofertas)
@Entity
@Table(name = "lotes_publicados")
data class Lote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var idProveedor: Long = 0,
    var nombreComercio: String = "",
    var nombreProducto: String = "",
    var descripcion: String = "",
    var categoria: String = "",

    var stock: Int = 0,
    var unidadesVendidas: Int = 0,

    // RF-06: desglose de precio (monto líquido + 10% comisión = precio final)
    var montoLiquido: Int = 0,
    var comision: Int = 0,
    var precioOfertaFinal: Int = 0,
    var precioNormal: Int = 0,

    var fechaCaducidad: LocalDateTime? = null,
    var finOferta: LocalDateTime? = null,
    var fechaPublicacion: LocalDateTime? = null,

    // RF-05: foto nativa tomada con la cámara, enviada en Base64 (JPEG)
    var fechaCapturaFoto: LocalDateTime? = null,
    @Column(length = 2_000_000)
    var fotoBase64: String? = null,

    var kgCo2EvitadoPorUnidad: Double = 0.0,
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var esAlcohol: Boolean = false,
    var activo: Boolean = true
)
