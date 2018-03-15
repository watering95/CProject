package com.example.water.cproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
class Genuino101 {
    private final BLE ble = new BLE();
    private final Gyroscope gyroscope = new Gyroscope();
    private final Accelerometer accelerometer = new Accelerometer();

    public Genuino101() {

    }

    public Gyroscope getGyroscope() {
        return this.gyroscope;
    }
    public Accelerometer getAccelerometer() {
        return this.accelerometer;
    }
    public BLE getBLE() {
        return this.ble;
    }
    public void bindService(Context context) {
        Intent gattServiceIntent = new Intent(context, IMUService.class);
        context.bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service){
            com.example.water.cproject.IMUService IMUService = ((com.example.water.cproject.IMUService.LocalBinder) service).getService();
            IMUService.initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0){
        }
    };
}
