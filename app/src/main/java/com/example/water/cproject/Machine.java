package com.example.water.cproject;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by water on 2017-04-19.
 */

class Machine {

    int MACHINE_FORWARD = 1;
    int MACHINE_BACKWARD = 4;
    int MACHINE_LEFT = 3;
    int MACHINE_RIGHT = 2;
    int MACHINE_STOP = 0;

    Genuino101 mGenuino;

    private BLEService mCommService;
    private Context mContext;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private int mRunSpeed;
    private int mLeftSpeed;
    private int mRightSpeed;
    private int mDirection;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic mMotorDirectionCharacteristic;
    private BluetoothGattCharacteristic mMotorLeftSpeedCharacteristic;
    private BluetoothGattCharacteristic mMotorRightSpeedCharacteristic;

    Machine(Context context) {
        mContext = context;
        mRunSpeed = 0;
        mDirection = MACHINE_STOP;
        mGenuino = new Genuino101();
        mCommService = new BLEService();
    }

    void setRunSpeed(int speed) {
        mRunSpeed = speed;
    }

    void setLeftSpeed(int speed) {
        mLeftSpeed = mRunSpeed + speed;
    }

    void setRightSpeed(int speed) {
        mRightSpeed = mRunSpeed + speed;
    }

    int getRunSpeed() {
        return mRunSpeed;
    }

    void action(int direction) {
        if(mMotorLeftSpeedCharacteristic != null) mCommService.writeCharacteristic(mMotorLeftSpeedCharacteristic, mLeftSpeed);
        if(mMotorLeftSpeedCharacteristic != null) mCommService.writeCharacteristic(mMotorRightSpeedCharacteristic, mRightSpeed);
        if(mMotorDirectionCharacteristic != null) mCommService.writeCharacteristic(mMotorDirectionCharacteristic, direction);
    }

    boolean commConnect(String address) {
        if (mCommService != null) {
            final boolean result = mCommService.connect(address);
            Log.d(TAG, "Connect request result=" + result);
            return result;
        }
        return false;
    }

    void commDisconnect() {
        mCommService.disconnect();
    }

    void bindService() {
        Intent gattServiceIntent = new Intent(mContext, BLEService.class);
        mContext.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    void registerGattReceiver(BroadcastReceiver gattUpdateReceiver) {
        mContext.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    void selectMachineGattServices() {
        selectGattServices(mCommService.getSupportedGattServices());
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void selectGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = mContext.getResources().getString(R.string.ble_unknown_service);
        String unknownCharaString = mContext.getResources().getString(R.string.ble_unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, gattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.equals(gattAttributes.UUID_MOTOR_DIRECTION)) {
                    mMotorDirectionCharacteristic = gattCharacteristic;
                } else if(uuid.equals(gattAttributes.UUID_MOTOR_RIGHT_SPEED)) {
                    mMotorLeftSpeedCharacteristic = gattCharacteristic;
                } else if(uuid.equals(gattAttributes.UUID_MOTOR_RIGHT_SPEED)) {
                    mMotorRightSpeedCharacteristic = gattCharacteristic;
                }

                currentCharaData.put(LIST_NAME, gattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service){
            mCommService = ((BLEService.LocalBinder) service).getService();
            if (!mCommService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
//            mCommService.connect(mServiceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0){
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
