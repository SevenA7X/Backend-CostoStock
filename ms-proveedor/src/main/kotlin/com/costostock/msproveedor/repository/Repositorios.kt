package com.costostock.msproveedor.repository

import com.costostock.msproveedor.model.FotoLote
import com.costostock.msproveedor.model.Publicacion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FotoLoteRepository : JpaRepository<FotoLote, Long> {
    fun findByIdLote(idLote: Long): FotoLote?
}

@Repository
interface PublicacionRepository : JpaRepository<Publicacion, Long> {
    fun findByIdProveedorOrderByIdDesc(idProveedor: Long): List<Publicacion>
}
