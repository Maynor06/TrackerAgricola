package com.example.demogps.Bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.example.demogps.Entitys.DeviceEntity

fun DeviceEntity.toBluetoothDevice(): BluetoothDevice? {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    if (bluetoothAdapter == null) {
        println("El dispositivo no tiene Bluetooth disponible.")
        return null
    }
    return try {
        bluetoothAdapter.getRemoteDevice(this.codigo)
    } catch (e: IllegalArgumentException) {
        println("Dirección MAC no válida: ${this.codigo}")
        null
    }
}
