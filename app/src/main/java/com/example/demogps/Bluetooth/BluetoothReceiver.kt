package com.example.demogps.Bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class BluetoothReceiver(private val dispositivosEncontrados: MutableList<BluetoothDevice>): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if(device != null && !dispositivosEncontrados.contains(device)){
                    dispositivosEncontrados.add(device)
                }
            }
        }
    }

    companion object {
        fun intentFilter() = IntentFilter(BluetoothDevice.ACTION_FOUND)
    }
}