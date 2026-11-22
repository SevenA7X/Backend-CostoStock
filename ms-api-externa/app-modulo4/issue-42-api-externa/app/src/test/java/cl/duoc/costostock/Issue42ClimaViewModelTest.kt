package cl.duoc.costostock

import cl.duoc.costostock.network.externa.ClimaActualDto
import cl.duoc.costostock.network.externa.ClimaApiService
import cl.duoc.costostock.network.externa.ClimaRespuestaDto
import cl.duoc.costostock.viewmodel.CiudadClima
import cl.duoc.costostock.viewmodel.ClimaViewModel
import cl.duoc.costostock.viewmodel.EstadoClima
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class Issue42ClimaViewModelTest : FunSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    test("API externa: muestra el clima de la ciudad seleccionada") {
        val api = mockk<ClimaApiService>()
        coEvery { api.obtenerClimaActual(any(), any(), any(), any()) } returns ClimaRespuestaDto(
            latitude = -33.04, longitude = -71.62, timezone = "America/Santiago",
            current = ClimaActualDto("2026-11-21T15:00", 18.5, 70, 12.0, 0, 1)
        )

        val vm = ClimaViewModel(api = api)
        vm.seleccionarCiudad(CiudadClima.SANTIAGO)

        val estado = vm.estado.value
        estado.shouldBeInstanceOf<EstadoClima.Exito>()
        estado.clima.ciudad shouldBe "Santiago"
        estado.clima.descripcion shouldBe "Despejado"
        coVerify { api.obtenerClimaActual(-33.4489, -70.6693, any(), any()) }
    }

    test("API externa: muestra error si no hay conexión") {
        val api = mockk<ClimaApiService>()
        coEvery { api.obtenerClimaActual(any(), any(), any(), any()) } throws RuntimeException("timeout")

        val vm = ClimaViewModel(api = api)

        vm.estado.value.shouldBeInstanceOf<EstadoClima.Error>()
    }
})
