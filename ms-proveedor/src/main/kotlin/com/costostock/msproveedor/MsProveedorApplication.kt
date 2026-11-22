package com.costostock.msproveedor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// Microservicio del portal del vendedor (issue #41): fotos de lotes y métricas de ventas
@SpringBootApplication
class MsProveedorApplication

fun main(args: Array<String>) {
    runApplication<MsProveedorApplication>(*args)
}
