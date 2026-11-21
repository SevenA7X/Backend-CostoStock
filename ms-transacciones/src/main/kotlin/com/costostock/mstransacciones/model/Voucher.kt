package com.costostock.mstransacciones.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

enum class EstadoVoucher {
    PENDIENTE, ENTREGADO, EXPIRADO_CANCELADO
}

@Entity
@Table(name = "vouchers")
data class Voucher(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val codigoQr: String = "",

    val idTransaccionPago: String = "",
    val idLote: Long = 0L,
    val idProveedor: Long = 0L,

    val compradorNombre: String = "",
    val compradorEmail: String = "",

    val cantidadUnidades: Int = 0,
    val montoTotalPagado: Double = 0.0,

    var kgCo2Evitado: Double = 0.0,

    val requiereVerificacionMayorEdad: Boolean = false,

    @Enumerated(EnumType.STRING)
    var estado: EstadoVoucher = EstadoVoucher.PENDIENTE,

    val fechaEmision: LocalDateTime = LocalDateTime.now(),
    var fechaEntrega: LocalDateTime? = null
)