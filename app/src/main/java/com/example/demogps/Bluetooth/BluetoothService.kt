package com.example.demogps.Bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID

class BluetoothService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null

    fun getPairedDevices(): Set<BluetoothDevice> {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ){
            return emptySet()
        }
        return bluetoothAdapter?.bondedDevices ?: emptySet()
    }

    fun connectToDevice(device: BluetoothDevice, onConnected: () -> Unit, onError: (Exception) -> Unit){
        Log.d("si entra aca", "si entra en el service")
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT ) != PackageManager.PERMISSION_GRANTED ){
            onError(SecurityException("No se tiene el permiso BLUETOOTH_CONNECT") )
            return
        }

        // Filtramos el UUID inválido
        val uuid = device.uuids?.firstOrNull {
            it.uuid != UUID.fromString("00000000-0000-1000-8000-00805f9b34fb")
        }?.uuid ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        Log.d("Bluetooth", "Intentando conectar con el UUID: $uuid")

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            Log.d("Bluetooth", "¡Conexión exitosa!")
            onConnected()
        } catch (e: IOException) {
            Log.e("Bluetooth", "Error de IO al conectar: ${e.message}")
            onError(e)
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Error de permisos: ${e.message}")
            onError(e)
        } catch (e: Exception) {
            Log.e("Bluetooth", "Error desconocido al conectar: ${e.message}")
            onError(e)
        }
    }


    fun sendCommand(command: String) {
        try {
            if (bluetoothSocket == null) throw NullPointerException("Socket Bluetooth es nulo")
            val outputStream = bluetoothSocket!!.outputStream
            outputStream.write(command.toByteArray())
            outputStream.flush()
            Log.d("Bluetoothc", "Comando enviado: $command")
        } catch (e: IOException) {
            Log.e("Bluetooth", "Error al enviar comando: ${e.message}")
            closeConnection()
            throw e
        }
    }


    fun receiveData(onDataReceived: (String) -> Unit, onError: (Exception) -> Unit) {
        Log.d("Bluetooth", "receiveData() fue llamada")

        if (bluetoothSocket == null) {
            onError(NullPointerException("Socket Bluetooth es nulo"))
            return
        }

        val inputStream = bluetoothSocket?.inputStream

        Thread {
            try {
                val buffer = ByteArray(1024)
                var bytes: Int

                while (bluetoothSocket?.isConnected == true) {
                    if (inputStream?.available() ?: 0 > 0) {
                        bytes = inputStream!!.read(buffer)
                        val data = String(buffer, 0, bytes)
                        Log.d("Bluetooth", "Datos recibidos: $data")

                        // Detectar comandos especiales
                        if (data.contains("EndConecction", ignoreCase = true)) {
                            Log.d("Bluetooth", "Fin de datos recibidos por el dispositivo")
                            onDataReceived(data.replace("EndConecction", "").trim())
                            break // o salir del loop de lectura sin error
                        }

                        onDataReceived(data)
                    } else {
                        Thread.sleep(200) // Reducir la carga en el CPU
                    }
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Error al leer datos: ${e.message}")
                onError(e)
            } catch (e: Exception) {
                Log.e("Bluetooth", "Error desconocido: ${e.message}")
                onError(e)
            } finally {
                Log.d("Bluetooth", "Finalizando hilo de recepción")
                closeConnection() // Cierra la conexión al finalizar
            }
        }.start()
    }




    fun closeConnection() {
        try {
            bluetoothSocket?.inputStream?.close()
            bluetoothSocket?.outputStream?.close()
            bluetoothSocket?.close()
            bluetoothSocket = null
            Log.d("Bluetooth", "Conexión Bluetooth cerrada correctamente")
        } catch (e: IOException) {
            Log.e("Bluetooth", "Error al cerrar la conexión: ${e.message}")
        }
    }


}