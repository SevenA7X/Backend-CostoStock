package com.costostock.mscore.repository

import com.costostock.mscore.model.Lote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoteRepository : JpaRepository<Lote, Long> {
    fun findByIdProveedor(idProveedor: Long): List<Lote>
}
