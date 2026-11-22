package cl.duoc.costostock

import cl.duoc.costostock.network.CoreApiService
import cl.duoc.costostock.network.MetricasVendedorDto
import cl.duoc.costostock.viewmodel.ProveedorViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class Issue41ProveedorViewModelTest : FunSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    test("RF-07: las métricas del vendedor se cargan desde MS-Core") {
        val api = mockk<CoreApiService>()
        coEvery { api.obtenerMetricas(1L) } returns MetricasVendedorDto(
            idProveedor = 1L, totalLotesPublicados = 3, lotesActivos = 2,
            totalUnidadesVendidas = 6, ventasBrutasTotales = 23100,
            montoLiquidoTotalAcumulado = 21000, comisionTotalGenerada = 2100,
            totalKgCo2Evitado = 42.0, equivalenciaArbolesPlantados = 2
        )

        val vm = ProveedorViewModel(coreApi = api)

        vm.metricas.value.totalLotesVendidosOComprados shouldBe 6
        vm.metricas.value.montoLiquidoTotalAcumulado shouldBe 21000
        vm.metricas.value.equivalenciaArbolesPlantados shouldBe 2
        vm.errorMetricas.value shouldBe null
    }

    test("RF-07: si MS-Core no responde se muestra un aviso y datos locales") {
        val api = mockk<CoreApiService>()
        coEvery { api.obtenerMetricas(any()) } throws RuntimeException("sin red")

        val vm = ProveedorViewModel(coreApi = api)

        vm.errorMetricas.value shouldBe "Sin conexión con MS-Core: mostrando datos locales"
    }

    test("RF-06: la comisión es el 10% del monto líquido") {
        val vm = ProveedorViewModel(coreApi = mockk(relaxed = true))
        vm.calcularComision(5000) shouldBe 500
    }
})
