package cl.duoc.costostock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.network.externa.ClimaActualDto
import cl.duoc.costostock.network.externa.ClimaApiService
import cl.duoc.costostock.network.externa.ClimaRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Ciudades que el usuario puede consultar
enum class CiudadClima(val nombre: String, val latitud: Double, val longitud: Double) {
    VALPARAISO("Valparaíso", -33.0458, -71.6197),
    VINA_DEL_MAR("Viña del Mar", -33.0245, -71.5518),
    SANTIAGO("Santiago", -33.4489, -70.6693)
}

// Modelo listo para mostrar en pantalla
data class ClimaUi(
    val ciudad: String,
    val temperatura: Double,
    val humedad: Int,
    val vientoKmH: Double,
    val descripcion: String,
    val icono: String,
    val esDeDia: Boolean,
    val horaMedicion: String,
    val consejoEcologico: String
)

sealed class EstadoClima {
    data object Cargando : EstadoClima()
    data class Exito(val clima: ClimaUi) : EstadoClima()
    data class Error(val mensaje: String) : EstadoClima()
}

class ClimaViewModel(
    private val api: ClimaApiService = ClimaRetrofitClient.api
) : ViewModel() {

    private val _ciudad = MutableStateFlow(CiudadClima.VALPARAISO)
    val ciudad: StateFlow<CiudadClima> = _ciudad.asStateFlow()

    private val _estado = MutableStateFlow<EstadoClima>(EstadoClima.Cargando)
    val estado: StateFlow<EstadoClima> = _estado.asStateFlow()

    init {
        cargarClima()
    }

    fun seleccionarCiudad(nueva: CiudadClima) {
        _ciudad.value = nueva
        cargarClima()
    }

    fun cargarClima() {
        viewModelScope.launch {
            _estado.value = EstadoClima.Cargando
            try {
                val c = _ciudad.value
                val respuesta = api.obtenerClimaActual(c.latitud, c.longitud)
                val actual = respuesta.current
                _estado.value = if (actual != null) {
                    EstadoClima.Exito(aClimaUi(c.nombre, actual))
                } else {
                    EstadoClima.Error("La API no devolvió datos")
                }
            } catch (e: Exception) {
                _estado.value = EstadoClima.Error("No se pudo consultar Open-Meteo. Revisa tu conexión a internet.")
            }
        }
    }

    private fun aClimaUi(ciudad: String, d: ClimaActualDto): ClimaUi {
        val (descripcion, icono) = describirCodigo(d.codigoClima, d.esDeDia == 1)
        return ClimaUi(
            ciudad = ciudad,
            temperatura = d.temperatura,
            humedad = d.humedad,
            vientoKmH = d.viento,
            descripcion = descripcion,
            icono = icono,
            esDeDia = d.esDeDia == 1,
            horaMedicion = d.time.substringAfter('T'),
            consejoEcologico = consejo(d.temperatura)
        )
    }

    // Códigos WMO que usa Open-Meteo
    private fun describirCodigo(codigo: Int, dia: Boolean): Pair<String, String> = when (codigo) {
        0 -> "Despejado" to if (dia) "☀️" else "🌙"
        1, 2 -> "Parcialmente nublado" to "⛅"
        3 -> "Nublado" to "☁️"
        45, 48 -> "Neblina" to "🌫️"
        in 51..57 -> "Llovizna" to "🌦️"
        in 61..67, in 80..82 -> "Lluvia" to "🌧️"
        in 71..77, 85, 86 -> "Nieve" to "❄️"
        in 95..99 -> "Tormenta" to "⛈️"
        else -> "Sin datos" to "🌡️"
    }

    // Tip ecológico según temperatura (relacionado con evitar desperdicio)
    private fun consejo(temperatura: Double): String = when {
        temperatura >= 25 -> "Hace calor: retira pronto tus congelados y lleva bolsa térmica."
        temperatura <= 8 -> "Día frío: buen momento para ir caminando a retirar tu lote."
        else -> "Clima templado: ve a pie o en bici a retirar tu lote y evita emisiones."
    }
}
