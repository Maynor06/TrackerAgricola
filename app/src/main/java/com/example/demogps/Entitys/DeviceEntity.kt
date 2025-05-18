package com.example.demogps.Entitys

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val codigo: String,

    val alias: String,


) {

}
