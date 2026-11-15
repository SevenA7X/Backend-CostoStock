package com.costostock.mstransacciones.repository

import com.costostock.mstransacciones.model.LoteOferta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoteOfertaRepository : JpaRepository<LoteOferta, Long> {
    fun findByActivoTrueAndStockGreaterThan(stockMinimo: Int): List<LoteOferta>
}