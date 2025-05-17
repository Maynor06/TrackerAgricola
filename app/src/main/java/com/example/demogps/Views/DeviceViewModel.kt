package com.example.demogps.Views

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.demogps.AppDatabase
import com.example.demogps.Bluetooth.BluetoothService
import com.example.demogps.Daos.DispositivoDao
import com.example.demogps.Entitys.CoordenadaEntity
import com.example.demogps.Entitys.DeviceEntity
import com.example.demogps.Entitys.RecorridoConCoordenadas
import com.example.demogps.Entitys.RecorridoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceViewModel(aplication: Application): AndroidViewModel(aplication) {
    private val db = AppDatabase.getInstance(aplication)
    private val bluetoothService = BluetoothService(getApplication())
    private val _connectionStatus = MutableStateFlow<String>("Desconectado")
    val deviceDao = db.dispositivoDao()
    val recorridoDao = db.recorridoDao()
    val coordenadaDao = db.coordenadaDao()
    val connectionStatus: StateFlow<String> = _connectionStatus


    val allDevices: LiveData<List<DeviceEntity>> = deviceDao.getAllDevices()

    fun insertarDevice(codigo: String, alias: String, descripcion: String){

        viewModelScope.launch {
            val device = DeviceEntity(codigo = codigo, alias = alias, descripcion = descripcion)
            deviceDao.insertDevice(device)
        }
    }

    fun insertarRecorrido(recorrido: RecorridoEntity) {
         viewModelScope.launch(Dispatchers.IO) {
             recorridoDao.insert(recorrido)
        }
    }

    fun conectarADispositivo(
        context: Context,
        device: BluetoothDevice,
        onSuccess: () -> Unit,
        OnError: (String) -> Unit
    ) {
        Log.d("SI entra para intentar conectarse ", "Intentando conexión...")

        val permisos = arrayOf(
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Permisos", "No tiene permisos, solicitando...")
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, permisos, 1)
            } else {
                OnError("Contexto no es una Activity, no se pueden solicitar permisos.")
            }
            return
        }

        bluetoothService.connectToDevice(
            device,
            onConnected = {
                _connectionStatus.value = "Conectado a ${device.name ?: device.address}"


                // 🔄 Iniciar recepción de datos de forma asíncrona
                bluetoothService.receiveData(
                    onDataReceived = { linea ->
                        viewModelScope.launch {
                            Log.d("Bluetooth", "Datos recibidos: $linea")
                            if (linea.isNotEmpty()) {
                                println("datos recibidos: $linea")
                            }
                        }
                    },
                    onError = { error ->
                        Log.e("Bluetooth", "Error al recibir datos: ${error.message}")
                        _connectionStatus.value = "Error al recibir datos: ${error.message}"
                    }
                )
                viewModelScope.launch {
                    delay(2000)
                    withContext(Dispatchers.Main){
                        onSuccess()
                    }
                }
            },
            onError = { e ->
                _connectionStatus.value = "Error: ${e.message}"
                OnError(e.message ?: "Error de conexión")
            }
        )

    }


    fun obtenerDispositivosPareados(context: Context): List<BluetoothDevice> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    return bluetoothAdapter.bondedDevices.toList()
                } else {
                    println("Permiso BLUETOOTH_CONNECT no concedido")
                    return emptyList()
                }
            } else {
                return bluetoothAdapter.bondedDevices.toList()
            }
        }
        return emptyList()
    }

    suspend fun obtenerRecorridosDelDispositivo(): List<String> {
        return withContext(Dispatchers.IO){
            try {
                enviarComando("3")

                val respuesta = recibirDatos()

                if(respuesta.isNotEmpty()) {
                    respuesta.split(",").map { it.trim() }
                }else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    fun obtenerRecorridosDeLaBd(deviceId: Long): LiveData<List<RecorridoEntity>> {
        return recorridoDao.obtenerRecorridosPorDispositivo(deviceId)
    }

    suspend fun obtenerCoordenadasDeRecorrido(nameRecorrido: String, recorridoId: Long){
        withContext(Dispatchers.IO){
            try {
                enviarComando("4:$nameRecorrido")

                val response = recibirDatos()

                if(!response.isNullOrEmpty()){
                    val coordenadas = response.split(";")

                    coordenadas.forEach{ coordenada ->
                        val datos = coordenada.split(",")
                        if(datos.size == 3){
                            val latitud = datos[0].toDouble()
                            val longitud = datos[1].toDouble()
                            val timestamp = datos[2].toLong()

                            val coordenadaEntity = CoordenadaEntity(
                                recorridoId = recorridoId,
                                latitude = latitud,
                                longitude = longitud,
                                timestamp = timestamp
                            )
                            coordenadaDao.insert(coordenadaEntity)
                        }
                    }

                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun sincronizarRecorridos(deviceId: Long, onComplete: (String) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val recorridosDelDispositivo = obtenerRecorridosDelDispositivo()
                val recorridoBd = obtenerRecorridosDeLaBd(deviceId).value ?: emptyList()

                val nombresEnBd = recorridoBd.map { it.date }
                val nuevosRecorridos = recorridosDelDispositivo.filter { it.first().toString() !in nombresEnBd }

                if(nuevosRecorridos.isNotEmpty()){
                    nuevosRecorridos.forEach { recorrido ->
                        val entity = RecorridoEntity(
                            deviceId = deviceId,
                            date = recorrido.first().toString(),
                        )
                        insertarRecorrido(entity)
                        obtenerCoordenadasDeRecorrido(recorrido, entity.id)
                    }
                    onComplete("Nuevos recorridos agregados!")
                }else {
                    onComplete("No hay recorridos nuevos")
                }
            } catch (e: Exception) {
                onComplete("Error al sincronizar: ${e.message}")
            }
        }
    }

    suspend fun recibirDatos(): String {
        return withContext(Dispatchers.IO){
            var response = ""
            bluetoothService.receiveData(
                onDataReceived = { linea ->
                    viewModelScope.launch {
                        Log.d("Bluetooth", "Datos recibidos: $linea")
                        if (linea.isNotEmpty()) {
                            println("datos recibidos: $linea")
                        }
                    }
                },
                onError = { error ->
                    Log.e("Bluetooth", "Error al recibir datos: ${error.message}")
                    _connectionStatus.value = "Error al recibir datos: ${error.message}"
                }
            )

            Thread.sleep(500)
            response
        }
    }

    fun enviarComando(comando: String){
        bluetoothService.sendCommand(comando)
    }

    fun cerrarConexion() {
        bluetoothService.closeConnection()
        _connectionStatus.value = "Desconectado"
    }


private fun Nothing?.insert(coordenada: CoordenadaEntity) {
    TODO("Not yet implemented")
}


}