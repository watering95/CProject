package com.example.water.cproject.genuino

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.water.cproject.ble.BLE
import com.example.water.cproject.machine.MachineService

/**
 * Created by water on 2017-04-18.
 */

class Genuino101 {
    val ble = BLE()
    val imu = IMU()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            val imuService = (service as MachineService.LocalBinder).service
            imuService.initialize()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    fun getBLE() : BLE {
        return ble
    }

    fun bindService(context: Context) {
        val gattServiceIntent = Intent(context, MachineService::class.java)
        context.bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }
}
