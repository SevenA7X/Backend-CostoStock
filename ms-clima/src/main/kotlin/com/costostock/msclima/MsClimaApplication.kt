package com.costostock.msclima

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// Microservicio de clima y ecología (issue #42): consume la API pública Open-Meteo
@SpringBootApplication
class MsClimaApplication

fun main(args: Array<String>) {
    runApplication<MsClimaApplication>(*args)
}
