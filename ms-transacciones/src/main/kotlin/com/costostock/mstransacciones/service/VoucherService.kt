package com.costostock.mstransacciones.service

import com.costostock.mstransacciones.model.EstadoVoucher
import com.costostock.mstransacciones.model.Voucher
import com.costostock.mstransacciones.repository.VoucherRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class VoucherService(
    private val repository: VoucherRepository
) {
    companion object {
        // Fórmula base: Asumimos que rescatar 1 unidad/kg de alimento evita 2.5 kg de CO2
        const val FACTOR_CO2_POR_UNIDAD = 2.5
    }

    // RF-14: Generar y emitir el Voucher digital
    fun generarVoucher(voucherReq: Voucher): Voucher {
        // Generamos un código único para el QR (Ej: CS-A1B2C3D4)
        val codigoGenerado = "CS-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"

        // Ejecución de la fórmula matemática para calcular el CO2 evitado
        val co2Evitado = voucherReq.cantidadUnidades * FACTOR_CO2_POR_UNIDAD

        val nuevoVoucher = voucherReq.copy(
            codigoQr = codigoGenerado,
            kgCo2Evitado = co2Evitado,
            estado = EstadoVoucher.PENDIENTE,
            fechaEmision = LocalDateTime.now()
        )

        return repository.save(nuevoVoucher)
    }

    // RF-16: Validar el código QR y marcar como entregado
    fun validarYEntregar(codigoQr: String, idProveedorReq: Long): Voucher {
        val voucher = repository.findByCodigoQr(codigoQr)
            ?: throw IllegalArgumentException("El código QR no existe en el sistema.")

        // Validaciones de seguridad replicando las que tenías en Android
        if (voucher.idProveedor != idProveedorReq) {
            throw IllegalArgumentException("Este voucher pertenece a otro comercio.")
        }
        if (voucher.estado == EstadoVoucher.ENTREGADO) {
            throw IllegalArgumentException("Este voucher ya fue entregado anteriormente.")
        }
        if (voucher.estado == EstadoVoucher.EXPIRADO_CANCELADO) {
            throw IllegalArgumentException("Este voucher está expirado o cancelado.")
        }

        // Si pasa todas las validaciones, actualizamos el estado
        voucher.estado = EstadoVoucher.ENTREGADO
        voucher.fechaEntrega = LocalDateTime.now()

        return repository.save(voucher)
    }
    // RF-17: Obtener el historial completo de vouchers emitidos
    fun obtenerHistorial(): List<Voucher> {
        return repository.findAllByOrderByFechaEmisionDesc()
    }
}