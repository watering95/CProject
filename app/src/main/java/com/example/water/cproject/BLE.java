package com.example.water.cproject;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.os.SystemClock.sleep;

/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
public class BLE {
    private String peripheralName = null;
    private String peripheralAddress = null;
    private boolean isConnected = false;

    private BLEService BLEService = new BLEService();
    private BluetoothGattCharacteristic characteristicNotify;

    BLE() {

    }

    void setPeripheralAddress(String str) {
        this.peripheralAddress = str;
    }
    String getPeripheralAddress() {
        return this.peripheralAddress;
    }
    boolean getConnectState() {
        return isConnected;
    }
    void setConnectState(boolean isConnected) {
        this.isConnected = isConnected;
    }

    void connect() {
        if (BLEService != null) {
            final boolean result = BLEService.connect(peripheralAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    void disconnect() {
        BLEService.disconnect();
    }
    void close() {
        BLEService.close();
    }

    void bindService(Context context) {
        Intent gattServiceIntent = new Intent(context, BLEService.class);
        context.bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }
    void writeCharacteristic(BluetoothGattCharacteristic characteristic, int data) {
        sleep(10);
        BLEService.writeCharacteristic(characteristic, data);
    }
    void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(characteristic != null) {
            final int charaProp = characteristic.getProperties();

            if((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                if (characteristicNotify != null) {
                    BLEService.setCharacteristicNotification(characteristicNotify, false);
                    characteristicNotify = null;
                }
                BLEService.readCharacteristic(characteristic);
            }
            if((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                characteristicNotify = characteristic;
                BLEService.setCharacteristicNotification(characteristic, true);
            }
        }
    }

    List<BluetoothGattService> getSupportedGattServices() {
        return BLEService.getSupportedGattServices();
    }
    IntentFilter makeGattUpdateIntentFilter() {
        return BLEService.makeGattUpdateIntentFilter();
    }

    public void setName(String str) {
        this.peripheralName = str;
    }
    public String getName() {
        return this.peripheralName;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service){
            BLEService = ((BLEService.LocalBinder) service).getService();
            if (BLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful on-up initialization.
//            BLEService.connect(mServiceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0){
            close();
        }
    };
}
