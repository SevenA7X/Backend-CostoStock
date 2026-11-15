package com.costostock.mstransacciones.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "lotes_oferta")
data class LoteOferta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var nombre: String = "",
    var descripcion: String = "",
    var categoria: String = "",
    var precioNormal: Double = 0.0,
    var precioOferta: Double = 0.0,
    var stock: Int = 0,
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var finOferta: LocalDateTime? = null,
    var fechaCaducidad: LocalDateTime? = null,
    var kgCo2Evitado: Double = 0.0,
    var esAlcohol: Boolean = false,
    var tiendaNombre: String = "",
    var activo: Boolean = true
)