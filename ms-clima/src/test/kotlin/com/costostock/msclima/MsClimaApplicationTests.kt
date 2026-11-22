package com.costostock.msclima

import com.costostock.msclima.service.ClimaTraductor
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class MsClimaApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun traduceLosCodigosWmoDeOpenMeteo() {
        assertEquals("Despejado" to "☀️", ClimaTraductor.describir(0, esDeDia = true))
        assertEquals("Despejado" to "🌙", ClimaTraductor.describir(0, esDeDia = false))
        assertEquals("Lluvia", ClimaTraductor.describir(63, esDeDia = true).first)
        assertEquals("Tormenta", ClimaTraductor.describir(95, esDeDia = true).first)
        assertEquals("Sin datos", ClimaTraductor.describir(999, esDeDia = true).first)
    }

    @Test
    fun elConsejoEcologicoDependeDeLaTemperatura() {
        assertTrue(ClimaTraductor.consejo(30.0).contains("congelados"))
        assertTrue(ClimaTraductor.consejo(5.0).contains("frío"))
        assertTrue(ClimaTraductor.consejo(18.0).contains("templado"))
    }
}
