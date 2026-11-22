package com.costostock.msproveedor.model

import jakarta.persistence.*
import java.time.LocalDateTime

// RF-05: foto nativa tomada con la cámara del celular, guardada como bytes (tabla fotos_lote)
@Entity
@Table(name = "fotos_lote")
data class FotoLote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var idProveedor: Long = 0,
    var idLote: Long? = null,          // id que asignó MS-Core al lote
    var contentType: String = "image/jpeg",
    var tamanoBytes: Int = 0,
    var fechaCaptura: LocalDateTime = LocalDateTime.now(),

    @Column(length = 3_000_000)
    var datos: ByteArray = ByteArray(0)
)

// Registro de cada publicación hecha desde el formulario del proveedor (tabla publicaciones)
@Entity
@Table(name = "publicaciones")
data class Publicacion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var idProveedor: Long = 0,
    var idLote: Long = 0,              // id del lote en MS-Core
    var nombreProducto: String = "",
    var categoria: String = "",
    var montoLiquido: Int = 0,
    var comision: Int = 0,
    var precioOfertaFinal: Int = 0,
    var idFoto: Long? = null,
    var fechaPublicacion: LocalDateTime = LocalDateTime.now()
)
