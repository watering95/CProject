package com.example.water.cproject.Genuino

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.water.cproject.BLE.BLE
import com.example.water.cproject.Machine.IMUService

/**
 * Created by water on 2017-04-18.
 */

class Genuino101 {
    val ble = BLE()
    val gyroscope = Gyroscope()
    val accelerometer = Accelerometer()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            val imuService = (service as com.example.water.cproject.Machine.IMUService.LocalBinder).service
            imuService.initialize()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    fun getBLE() : BLE {
        return ble
    }

    fun bindService(context: Context) {
        val gattServiceIntent = Intent(context, IMUService::class.java)
        context.bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }
}
