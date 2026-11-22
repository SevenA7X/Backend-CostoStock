package cl.duoc.costostock.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.costostock.viewmodel.CiudadClima
import cl.duoc.costostock.viewmodel.ClimaUi
import cl.duoc.costostock.viewmodel.ClimaViewModel
import cl.duoc.costostock.viewmodel.EstadoClima

// Vista dedicada a la API externa (Guía 14). No usa los microservicios Spring Boot.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimaScreen(viewModel: ClimaViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val ciudad by viewModel.ciudad.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Clima y ecología") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector de ciudad
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CiudadClima.entries.forEach { c ->
                    FilterChip(
                        selected = c == ciudad,
                        onClick = { viewModel.seleccionarCiudad(c) },
                        label = { Text(c.nombre) }
                    )
                }
            }

            AnimatedContent(targetState = estado, label = "estadoClima") { e ->
                when (e) {
                    is EstadoClima.Cargando -> Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text("Consultando Open-Meteo...")
                    }
                    is EstadoClima.Error -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MensajeError(e.mensaje)
                        Button(onClick = { viewModel.cargarClima() }) { Text("Reintentar") }
                    }
                    is EstadoClima.Exito -> TarjetaClima(e.clima)
                }
            }

            Button(onClick = { viewModel.cargarClima() }, modifier = Modifier.fillMaxWidth()) {
                Text("Actualizar")
            }

            Text(
                "Datos: Open-Meteo.com (API pública externa, CC BY 4.0)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaClima(clima: ClimaUi) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(clima.icono, fontSize = 64.sp)
                Text("${"%.1f".format(clima.temperatura)} °C", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text(clima.descripcion, style = MaterialTheme.typography.titleMedium)
                Text("${clima.ciudad} · medición ${clima.horaMedicion}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DatoClima("💧 Humedad", "${clima.humedad} %", Modifier.weight(1f))
            DatoClima("💨 Viento", "${"%.0f".format(clima.vientoKmH)} km/h", Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("🌱 Consejo CostoStock", fontWeight = FontWeight.Bold)
                Text(clima.consejoEcologico)
            }
        }
    }
}

@Composable
private fun DatoClima(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodySmall)
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
