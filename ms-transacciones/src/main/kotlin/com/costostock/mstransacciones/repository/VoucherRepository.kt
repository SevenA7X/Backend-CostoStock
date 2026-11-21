package com.costostock.mstransacciones.repository

import com.costostock.mstransacciones.model.Voucher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoucherRepository : JpaRepository<Voucher, Long> {
    fun findByCodigoQr(codigoQr: String): Voucher?
    fun findAllByOrderByFechaEmisionDesc(): List<Voucher>
}