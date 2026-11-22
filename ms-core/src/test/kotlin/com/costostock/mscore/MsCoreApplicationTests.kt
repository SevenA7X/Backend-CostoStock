package com.costostock.mscore

import com.costostock.mscore.controller.LoteController
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class MsCoreApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun comisionDel10PorCientoSeSumaAlPrecioFinal() {
        val desglose = LoteController.calcularPrecioFinal(5000)
        assertEquals(500, desglose.comision)
        assertEquals(5500, desglose.precioFinal)
    }
}
