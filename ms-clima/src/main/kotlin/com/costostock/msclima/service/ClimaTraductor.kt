package com.costostock.msclima.service

// Convierte los datos numéricos de Open-Meteo en textos para el usuario
object ClimaTraductor {

    // Códigos WMO que usa Open-Meteo -> (descripción, ícono)
    fun describir(codigo: Int, esDeDia: Boolean): Pair<String, String> = when (codigo) {
        0 -> "Despejado" to if (esDeDia) "☀️" else "🌙"
        1, 2 -> "Parcialmente nublado" to "⛅"
        3 -> "Nublado" to "☁️"
        45, 48 -> "Neblina" to "🌫️"
        in 51..57 -> "Llovizna" to "🌦️"
        in 61..67, in 80..82 -> "Lluvia" to "🌧️"
        in 71..77, 85, 86 -> "Nieve" to "❄️"
        in 95..99 -> "Tormenta" to "⛈️"
        else -> "Sin datos" to "🌡️"
    }

    // Consejo ecológico de CostoStock según la temperatura
    fun consejo(temperatura: Double): String = when {
        temperatura >= 25 -> "Hace calor: retira pronto tus congelados y lleva bolsa térmica."
        temperatura <= 8 -> "Día frío: buen momento para ir caminando a retirar tu lote."
        else -> "Clima templado: ve a pie o en bici a retirar tu lote y evita emisiones."
    }
}
