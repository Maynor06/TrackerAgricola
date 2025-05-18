package com.example.demogps.Compose.UX

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demogps.R
import com.example.demogps.Views.DeviceViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.demogps.Bluetooth.BluetoothService
import com.example.demogps.Entitys.DeviceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainNavigation(viewModel: DeviceViewModel){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "servicios"){
        composable("servicios"){
            ServiciosScreen(viewModel, dispositivosEncontrados = listOf(), navController)
        }

        composable("recorridos/{deviceName}/{deviceId}",
                arguments = listOf(
                    navArgument("deviceName") { type = NavType.StringType },
                    navArgument("deviceId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: "Desconocido"
            val deviceId = backStackEntry.arguments?.getLong("deviceId") ?: 0L
            RecorridosScreen(deviceName = deviceName, deviceId = deviceId, viewModel = viewModel)
        }
    }
}

fun solicitarPermisosBluetooth(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                    1 // Puedes cambiar este request code si lo necesitas
                )
            }
        }
    }
}


@Composable
fun ServiciosScreen(viewModel: DeviceViewModel, dispositivosEncontrados: List<BluetoothDevice>, navController: NavController) {
    val devices by viewModel.allDevices.observeAsState(emptyList())
    val context = LocalContext.current
    val deviceIdE = 1L;
    val pairedDevices = remember { viewModel.obtenerDispositivosPareados(context) }
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var dispositivosEncontrados by remember { mutableStateOf(listOf<BluetoothDevice>()) }


    val receiver = remember {
        object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if(BluetoothDevice.ACTION_FOUND == intent?.action ){
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && !dispositivosEncontrados.contains(device)){
                        dispositivosEncontrados = dispositivosEncontrados + device
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = android.content.IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Verificación de permisos
    val permisosConcedidos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Para versiones anteriores, no se necesita el permiso
    }

    LaunchedEffect(Unit) {
        solicitarPermisosBluetooth(context)
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        headerMain()

        Spacer(modifier = Modifier.height(30.dp))

        // title
        Text(
            text = "Dispositivos conectados:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 30.dp)
        )

        // 💡 Dispositivos emparejados
        if (pairedDevices.isNotEmpty()) {
            pairedDevices.forEach { device ->
                val deviceName = if (permisosConcedidos) device.name ?: "Desconocido" else "Permiso denegado"
                val deviceAddress = if (permisosConcedidos) device.address else "Permiso denegado"
                Log.d("datos del dispositivo", "name: ${device.name}, address: ${device.address}")

                CardDevice(
                    device = device,
                    codigo = deviceAddress,
                    alias = deviceName,
                    description = "Bluetooth Device",
                    onConnect = { device, onSuccess, OnError ->
                        viewModel.conectarADispositivo(
                            context = context,
                            device = device,
                            onSuccess = {
                                onSuccess()
                                Log.d("NavigationDebug", "Navegando a recorridos con deviceName=$deviceName y deviceId=$deviceIdE")
                                val safeDevice = deviceName ?: "Desconocido"
                                val safeDeviceAdress = deviceAddress ?: "Sin direccion"
                                try {
                                    navController.navigate("recorridos/${deviceName}/${deviceIdE}")
                                } catch (e: Exception){
                                    Log.e("NavigationError", "Error al navegar: ${e.message}")
                                }
                            },
                            OnError = { mensaje ->
                                OnError(mensaje)
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            Text("No hay dispositivos pareados.", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 💡 Dispositivos encontrados
        if (dispositivosEncontrados.isNotEmpty()) {
            dispositivosEncontrados.forEach { device ->
                val deviceName = if (permisosConcedidos) device.name ?: "Desconocido" else "Permiso denegado"
                val deviceAddress = if (permisosConcedidos) device.address else "Permiso denegado"

                CardDevice(
                    device = device,
                    codigo = deviceAddress,
                    alias = deviceName,
                    description = "Bluetooth Device",
                    onConnect = { device, onSuccess, OnError ->
                        viewModel.conectarADispositivo(
                            context = context,
                            device = device,
                            onSuccess = {
                                onSuccess()

                                navController.navigate("recorridos/${deviceName ?: "Desconocido"}/${deviceAddress}")
                            },
                            OnError = { mensaje ->
                                OnError(mensaje)
                            }
                        )
                    }
                )

            }
        } else {
            Text("No se encontraron dispositivos cercanos.", color = Color.Gray)
        }
    }
}


@Composable
fun headerMain() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Imagen izquierda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(30.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Perfil",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = "Agrotech",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CardDevice(
    device: BluetoothDevice,
    codigo: String,
    alias: String,
    description: String,
    onConnect: (BluetoothDevice, () -> Unit, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE9FBE9)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = codigo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alias,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        onConnect(device,
                            {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "Conectado exitosamente a $alias", Toast.LENGTH_SHORT).show()
                                }
                            },
                            { mensaje ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )


                    }
                }
            ) {
                Text(text = "Conectarse")
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}
