package com.example.demogps.Compose.UX

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.map
import com.example.demogps.Entitys.RecorridoEntity
import com.example.demogps.Views.DeviceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RecorridosScreen(deviceName: String, deviceId: Long, viewModel: DeviceViewModel) {
    val context = LocalContext.current
    val recorridos by viewModel.obtenerRecorridosDeLaBd(deviceId).observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.sincronizarRecorridos(deviceId){ mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Título principal
        Text(
            text = "Recorridos del dispositivo $deviceName",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C),
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        // Lista de recorridos
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recorridos) { recorrido ->
               cardRecorrido(recorrido)

            }
        }
    }
    buttonAddRecorrido(viewModel, 1)
}

@Composable
fun cardRecorrido(recorrido: RecorridoEntity){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fecha del recorrido:",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = recorrido.date.toString(),
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Nombre del recorrido:",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = recorrido.id.toString(),
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun buttonAddRecorrido(deviceViewModel: DeviceViewModel, deviceId:Long ){
    val context = LocalContext.current

    Button(
        onClick = {
            CoroutineScope(Dispatchers.IO).launch {

                val recorridosDelDispositivo = deviceViewModel.obtenerRecorridosDelDispositivo()

                val recorridoBd = withContext(Dispatchers.IO) {
                    deviceViewModel.obtenerRecorridosDeLaBd(deviceId).value ?: emptyList()
                }

                val nombresEnBd = recorridoBd.map { it.date }
                val nuevosRecorridos = recorridosDelDispositivo.filter { it !in nombresEnBd }

                if(nuevosRecorridos.isNotEmpty()){
                    nuevosRecorridos.forEach { recorrido ->
                        val entity = RecorridoEntity(
                            deviceId = deviceId,
                            date = recorrido.first().toString()
                        )
                        deviceViewModel.insertarRecorrido(entity)
                        val id = 1 // esto lo vamos a cambiar.
                        deviceViewModel.obtenerCoordenadasDeRecorrido(recorrido, id.toLong())
                    }
                    Toast.makeText(context, "Nuevos recorridos agregados!", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "No hay recorridos nuevos", Toast.LENGTH_SHORT ).show()
                }
            }
        },
    ) {
        Text("Agregar nuevo recorrido")
    }
}