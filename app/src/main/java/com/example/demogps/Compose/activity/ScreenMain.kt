package com.example.demogps.Compose.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.demogps.Bluetooth.BluetoothReceiver
import com.example.demogps.Compose.UX.MainNavigation
import com.example.demogps.Compose.UX.ServiciosScreen
import com.example.demogps.Compose.ui.theme.DemoGPSTheme
import com.example.demogps.Views.DeviceViewModel

class ScreenMain : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val dispositivosEncontrados = mutableListOf<BluetoothDevice>()
    private var receiver: BluetoothReceiver? = null

    // 🔹 Permisos según la versión de Android
    private val permisosBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // 🔹 Lanzador para los permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            iniciarEscaneo()
        }
    }

    // 🔹 Ciclo de vida
    override fun onStart() {
        super.onStart()
        if (!tienePermisos()) {
            requestPermissionLauncher.launch(permisosBluetooth)
        } else {
            iniciarEscaneo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
        receiver?.let {
            unregisterReceiver(it)
        }
    }

    // 🔹 Verificación de permisos
    private fun tienePermisos(): Boolean {
        return permisosBluetooth.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 🔹 Escaneo de dispositivos
    @SuppressLint("MissingPermission")
    private fun iniciarEscaneo() {
        if (tienePermisos()) {
            bluetoothAdapter?.let {
                if (!it.isDiscovering) {
                    it.startDiscovery()
                }
            }
            receiver = BluetoothReceiver(dispositivosEncontrados)
            registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        } else {
            requestPermissionLauncher.launch(permisosBluetooth)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[DeviceViewModel::class.java]

        setContent {
            DemoGPSTheme {
                MainNavigation(viewModel = viewModel) // Cambiamos a MainNavigation
            }
        }
    }

}

class DeviceViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
