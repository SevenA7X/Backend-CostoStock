package com.costostock.msclima.repository

import com.costostock.msclima.model.ConsultaClima
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsultaClimaRepository : JpaRepository<ConsultaClima, Long> {
    // Última consulta guardada de una ciudad (caché)
    fun findFirstByCiudadOrderByFechaConsultaDesc(ciudad: String): ConsultaClima?

    // Últimas 20 consultas de una ciudad (historial)
    fun findTop20ByCiudadOrderByFechaConsultaDesc(ciudad: String): List<ConsultaClima>
}
