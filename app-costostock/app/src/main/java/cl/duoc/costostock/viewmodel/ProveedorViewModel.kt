package cl.duoc.costostock.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.model.CategoriaProducto
import cl.duoc.costostock.model.CompradorLigero
import cl.duoc.costostock.model.EstadoVoucher
import cl.duoc.costostock.model.ResumenImpactoMetricas
import cl.duoc.costostock.model.VoucherRetiro
import cl.duoc.costostock.network.CoreApiService
import cl.duoc.costostock.network.PublicarLoteRequest
import cl.duoc.costostock.network.RetrofitClient
import cl.duoc.costostock.util.aBase64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

enum class CampoLote { NOMBRE, DESCRIPCION, CATEGORIA, STOCK, MONTO, PRECIO_NORMAL, CADUCIDAD, VIGENCIA, FOTO }

data class PublicarLoteUiState(
    val nombreProducto: String = "",
    val descripcion: String = "",
    val categoria: CategoriaProducto? = null,
    val stockTexto: String = "",
    val montoLiquidoTexto: String = "",
    val precioNormalTexto: String = "",
    val caducidadTexto: String = "",
    val vigenciaHorasTexto: String = "",
    val fotoCapturada: Bitmap? = null,
    val fechaCapturaFotoMillis: Long? = null,
    val comision: Int = 0,
    val precioOfertaFinal: Int = 0,
    val errores: Map<CampoLote, String> = emptyMap(),
    val publicado: Boolean = false,
    // --- NUEVO (RF-05): estado del envío al backend ---
    val enviando: Boolean = false,
    val errorEnvio: String? = null,
    val idLotePublicado: Long? = null
)

sealed class ResultadoVoucher {
    data object SinValidar : ResultadoVoucher()
    data class Valido(val voucher: VoucherRetiro) : ResultadoVoucher()
    data class Invalido(val motivo: String) : ResultadoVoucher()
    data class Entregado(val voucher: VoucherRetiro) : ResultadoVoucher()
}

// NUEVO: recibe la API de MS-Core (por defecto la de RetrofitClient).
// Al tener valor por defecto, viewModel() sigue funcionando sin Factory.
class ProveedorViewModel(
    private val coreApi: CoreApiService = RetrofitClient.coreApi
) : ViewModel() {

    companion object {
        const val PORCENTAJE_COMISION = 0.10
        const val KG_CO2_POR_ARBOL_AL_ANIO = 21.0
        private val PALABRAS_FRESCAS = listOf(
            "fruta", "verdura", "carne", "pollo", "pescado", "marisco",
            "lechuga", "tomate", "palta", "fresco", "fresca"
        )

        // Datos del comercio que publica (demo, igual que idProveedorActual)
        const val NOMBRE_COMERCIO = "Minimarket Demo"
        const val LATITUD_TIENDA = -33.0458
        const val LONGITUD_TIENDA = -71.6197
    }

    // =====================================================================
    //  PUBLICAR LOTE
    // =====================================================================
    private val _publicarState = MutableStateFlow(PublicarLoteUiState())
    val publicarState: StateFlow<PublicarLoteUiState> = _publicarState.asStateFlow()

    fun onNombreChange(valor: String) = _publicarState.update {
        it.copy(nombreProducto = valor).sinError(CampoLote.NOMBRE)
    }

    fun onDescripcionChange(valor: String) = _publicarState.update {
        it.copy(descripcion = valor).sinError(CampoLote.DESCRIPCION)
    }

    fun onCategoriaChange(valor: CategoriaProducto) = _publicarState.update {
        it.copy(categoria = valor).sinError(CampoLote.CATEGORIA)
    }

    fun onStockChange(valor: String) = _publicarState.update {
        it.copy(stockTexto = valor.filter { c -> c.isDigit() }.take(6)).sinError(CampoLote.STOCK)
    }

    fun onMontoChange(valor: String) {
        val limpio = valor.filter { it.isDigit() }.take(9)
        val monto = limpio.toIntOrNull() ?: 0
        val comision = calcularComision(monto)
        _publicarState.update {
            it.copy(
                montoLiquidoTexto = limpio,
                comision = comision,
                precioOfertaFinal = if (monto > 0) monto + comision else 0
            ).sinError(CampoLote.MONTO)
        }
    }

    fun onPrecioNormalChange(valor: String) = _publicarState.update {
        it.copy(precioNormalTexto = valor.filter { c -> c.isDigit() }.take(9)).sinError(CampoLote.PRECIO_NORMAL)
    }

    fun onCaducidadChange(valor: String) = _publicarState.update {
        it.copy(caducidadTexto = valor.filter { c -> c.isDigit() || c == '/' }.take(10)).sinError(CampoLote.CADUCIDAD)
    }

    fun onVigenciaChange(valor: String) = _publicarState.update {
        it.copy(vigenciaHorasTexto = valor.filter { c -> c.isDigit() }.take(4)).sinError(CampoLote.VIGENCIA)
    }

    fun onFotoCapturada(bitmap: Bitmap) = _publicarState.update {
        it.copy(fotoCapturada = bitmap, fechaCapturaFotoMillis = System.currentTimeMillis())
            .sinError(CampoLote.FOTO)
    }

    fun calcularComision(montoLiquido: Int): Int = (montoLiquido * PORCENTAJE_COMISION).roundToInt()

    fun validarLote(s: PublicarLoteUiState): Map<CampoLote, String> {
        val errores = mutableMapOf<CampoLote, String>()
        if (s.nombreProducto.isBlank()) {
            errores[CampoLote.NOMBRE] = "El nombre del producto es obligatorio"
        } else if (esAlimentoFresco(s.nombreProducto)) {
            errores[CampoLote.NOMBRE] = "No se permiten alimentos frescos"
        }

        if (s.descripcion.isBlank()) errores[CampoLote.DESCRIPCION] = "La descripción es obligatoria"
        if (s.categoria == null) errores[CampoLote.CATEGORIA] = "Seleccione una categoría"

        val stock = s.stockTexto.toIntOrNull()
        if (stock == null) errores[CampoLote.STOCK] = "El stock es obligatorio"
        else if (stock <= 0) errores[CampoLote.STOCK] = "El stock debe ser mayor a 0"

        val monto = s.montoLiquidoTexto.toIntOrNull()
        if (monto == null) errores[CampoLote.MONTO] = "El monto líquido es obligatorio"
        else if (monto <= 0) errores[CampoLote.MONTO] = "El monto líquido debe ser mayor a 0"

        val precioNormal = s.precioNormalTexto.toIntOrNull()
        if (precioNormal == null) errores[CampoLote.PRECIO_NORMAL] = "El precio normal es obligatorio"
        else if (precioNormal <= 0) errores[CampoLote.PRECIO_NORMAL] = "El precio normal debe ser mayor a 0"
        else if (s.precioOfertaFinal > 0 && precioNormal <= s.precioOfertaFinal)
            errores[CampoLote.PRECIO_NORMAL] = "Debe ser mayor al precio de oferta (${s.precioOfertaFinal})"

        if (s.caducidadTexto.isBlank()) {
            errores[CampoLote.CADUCIDAD] = "La fecha de caducidad es obligatoria"
        } else {
            val fecha = parsearFecha(s.caducidadTexto)
            if (fecha == null) errores[CampoLote.CADUCIDAD] = "Use el formato dd/mm/aaaa"
            else if (fecha <= System.currentTimeMillis()) errores[CampoLote.CADUCIDAD] = "La fecha debe ser futura"
        }

        val vigencia = s.vigenciaHorasTexto.toIntOrNull()
        if (vigencia == null) errores[CampoLote.VIGENCIA] = "La vigencia de la promoción es obligatoria"
        else if (vigencia <= 0) errores[CampoLote.VIGENCIA] = "La vigencia debe ser mayor a 0 horas"

        if (s.fotoCapturada == null) errores[CampoLote.FOTO] = "Debe tomar una foto actual del lote"
        return errores
    }

    private fun parsearFecha(texto: String): Long? = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(texto)?.time
    } catch (e: ParseException) {
        null
    }

    // "31/12/2026" -> "2026-12-31" (formato que espera MS-Core)
    private fun aFechaIso(texto: String): String {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(texto)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(fecha!!)
    }

    private fun esAlimentoFresco(nombre: String): Boolean {
        val texto = nombre.lowercase()
        return PALABRAS_FRESCAS.any { texto.contains(it) }
    }

    // RF-05: valida localmente y, si todo está OK, envía el lote al backend vía Retrofit
    fun publicarLote(): Boolean {
        val estado = _publicarState.value
        val errores = validarLote(estado)
        _publicarState.update { it.copy(errores = errores, publicado = false, errorEnvio = null) }
        if (errores.isNotEmpty()) return false

        enviarLoteAlBackend(estado)
        return true
    }

    private fun enviarLoteAlBackend(s: PublicarLoteUiState) {
        viewModelScope.launch {
            _publicarState.update { it.copy(enviando = true, errorEnvio = null) }
            try {
                val request = PublicarLoteRequest(
                    idProveedor = idProveedorActual,
                    nombreComercio = NOMBRE_COMERCIO,
                    nombreProducto = s.nombreProducto.trim(),
                    descripcion = s.descripcion.trim(),
                    categoria = s.categoria!!.name,
                    stock = s.stockTexto.toInt(),
                    montoLiquido = s.montoLiquidoTexto.toInt(),
                    precioNormal = s.precioNormalTexto.toInt(),
                    fechaCaducidad = aFechaIso(s.caducidadTexto),
                    vigenciaHoras = s.vigenciaHorasTexto.toInt(),
                    fotoBase64 = s.fotoCapturada?.aBase64(),   // foto nativa -> Base64
                    latitud = LATITUD_TIENDA,
                    longitud = LONGITUD_TIENDA
                )

                val respuesta = coreApi.publicarLote(request)

                if (respuesta.isSuccessful) {
                    val creado = respuesta.body()
                    _publicarState.update {
                        it.copy(
                            enviando = false,
                            publicado = true,
                            idLotePublicado = creado?.id,
                            // usamos los valores calculados por el servidor (RF-06)
                            comision = creado?.comision ?: it.comision,
                            precioOfertaFinal = creado?.precioOfertaFinal ?: it.precioOfertaFinal
                        )
                    }
                    cargarMetricasDesdeBackend()
                } else {
                    // MS-Core responde { "error": "..." } cuando rechaza el lote (ej. RF-04)
                    val mensaje = leerMensajeError(respuesta.errorBody()?.string())
                    _publicarState.update { it.copy(enviando = false, errorEnvio = mensaje) }
                }
            } catch (e: Exception) {
                _publicarState.update {
                    it.copy(enviando = false, errorEnvio = "No se pudo conectar con MS-Core (puerto 8080)")
                }
            }
        }
    }

    private fun leerMensajeError(cuerpo: String?): String = try {
        JSONObject(cuerpo ?: "").getString("error")
    } catch (e: Exception) {
        "El servidor rechazó el lote"
    }

    fun reiniciarFormulario() {
        _publicarState.value = PublicarLoteUiState()
    }

    private fun PublicarLoteUiState.sinError(campo: CampoLote) =
        copy(errores = errores - campo, publicado = false, errorEnvio = null)

    // =====================================================================
    //  VALIDAR VOUCHER + MÉTRICAS DEL VENDEDOR
    // =====================================================================
    var idProveedorActual: Long = 1L
    private val _vouchers = MutableStateFlow(vouchersDemo())
    private val _resultadoVoucher = MutableStateFlow<ResultadoVoucher>(ResultadoVoucher.SinValidar)
    val resultadoVoucher: StateFlow<ResultadoVoucher> = _resultadoVoucher.asStateFlow()

    // RF-07: las métricas ahora vienen de MS-Core
    private val _metricas = MutableStateFlow(ResumenImpactoMetricas(0.0, 0, 0, 0))
    val metricas: StateFlow<ResumenImpactoMetricas> = _metricas.asStateFlow()

    private val _cargandoMetricas = MutableStateFlow(false)
    val cargandoMetricas: StateFlow<Boolean> = _cargandoMetricas.asStateFlow()

    private val _errorMetricas = MutableStateFlow<String?>(null)
    val errorMetricas: StateFlow<String?> = _errorMetricas.asStateFlow()

    init {
        cargarMetricasDesdeBackend()
    }

    fun cargarMetricasDesdeBackend() {
        viewModelScope.launch {
            _cargandoMetricas.value = true
            _errorMetricas.value = null
            try {
                _metricas.value = coreApi.obtenerMetricas(idProveedorActual).toDomainModel()
            } catch (e: Exception) {
                // Si el servidor no responde, mostramos el cálculo local como respaldo
                _metricas.value = calcularMetricas(_vouchers.value)
                _errorMetricas.value = "Sin conexión con MS-Core: mostrando datos locales"
            } finally {
                _cargandoMetricas.value = false
            }
        }
    }

    fun validarCodigoQR(codigo: String) {
        val limpio = codigo.trim()
        val voucher = _vouchers.value.firstOrNull { it.codigoOrdenQR == limpio }
        _resultadoVoucher.value = when {
            limpio.isEmpty() -> ResultadoVoucher.Invalido("Ingrese o escanee un código")
            voucher == null -> ResultadoVoucher.Invalido("El código no existe")
            voucher.idProveedor != idProveedorActual ->
                ResultadoVoucher.Invalido("Este voucher pertenece a otro comercio")
            voucher.estadoVoucher == EstadoVoucher.ENTREGADO ->
                ResultadoVoucher.Invalido("Este voucher ya fue entregado")
            voucher.estadoVoucher == EstadoVoucher.EXPIRADO_CANCELADO ->
                ResultadoVoucher.Invalido("Este voucher está expirado o cancelado")
            else -> ResultadoVoucher.Valido(voucher)
        }
    }

    fun confirmarEntrega() {
        val actual = _resultadoVoucher.value as? ResultadoVoucher.Valido ?: return
        val entregado = actual.voucher.copy(
            estadoVoucher = EstadoVoucher.ENTREGADO,
            fechaEntregaConfirmadaMillis = System.currentTimeMillis()
        )
        _vouchers.update { lista ->
            lista.map { if (it.idVoucher == entregado.idVoucher) entregado else it }
        }
        _resultadoVoucher.value = ResultadoVoucher.Entregado(entregado)
        cargarMetricasDesdeBackend()   // refresca las métricas desde el servidor
    }

    fun reiniciarValidacion() {
        _resultadoVoucher.value = ResultadoVoucher.SinValidar
    }

    // Cálculo local: sólo se usa como respaldo si MS-Core no responde
    fun calcularMetricas(vouchers: List<VoucherRetiro>): ResumenImpactoMetricas {
        val entregados = vouchers.filter { it.estadoVoucher == EstadoVoucher.ENTREGADO }
        val kg = entregados.sumOf { it.kgCo2Evitado }
        return ResumenImpactoMetricas(
            totalKgCo2Evitado = kg,
            equivalenciaArbolesPlantados = (kg / KG_CO2_POR_ARBOL_AL_ANIO).roundToInt(),
            totalLotesVendidosOComprados = entregados.size,
            montoLiquidoTotalAcumulado = entregados.sumOf { it.subtotalPagado }
        )
    }

    // Datos de prueba para la validación de vouchers (se mantienen de la Ev. 2)
    private fun vouchersDemo(): List<VoucherRetiro> = listOf(
        VoucherRetiro(
            idVoucher = 1L, codigoOrdenQR = "CS-DEMO-0001", idTransaccionPago = "TX-0001",
            comprador = CompradorLigero("Ana Pérez", "ana@correo.cl", "+56911112222"),
            idLote = 1L, nombreProducto = "Arroz 1 kg (pack x6)", cantidadUnidades = 2,
            subtotalPagado = 6000, comisionPagada = 600, montoTotalPagado = 6600,
            idProveedor = 1L, nombreComercio = "Minimarket Demo",
            direccionExactaRetiro = "Av. Principal 123", coordenadasGpsRetiro = "-33.0000,-71.0000",
            horarioValidoRetiro = "09:00 - 20:00", kgCo2Evitado = 1.8,
            requiereVerificacionMayorEdad = false, fechaEmisionMillis = System.currentTimeMillis()
        ),
        VoucherRetiro(
            idVoucher = 2L, codigoOrdenQR = "CS-DEMO-0002", idTransaccionPago = "TX-0002",
            comprador = CompradorLigero("Luis Rojas", "luis@correo.cl", "+56922223333"),
            idLote = 2L, nombreProducto = "Vino tinto 750 ml", cantidadUnidades = 1,
            subtotalPagado = 4500, comisionPagada = 450, montoTotalPagado = 4950,
            idProveedor = 1L, nombreComercio = "Minimarket Demo",
            direccionExactaRetiro = "Av. Principal 123", coordenadasGpsRetiro = "-33.0000,-71.0000",
            horarioValidoRetiro = "09:00 - 20:00", kgCo2Evitado = 0.9,
            requiereVerificacionMayorEdad = true, fechaEmisionMillis = System.currentTimeMillis()
        )
    )
}
