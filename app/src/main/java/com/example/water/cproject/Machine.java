package com.example.water.cproject;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.IntentFilter;

import java.util.List;

/**
 * Created by water on 2017-04-19.
 */

@SuppressWarnings("DefaultFileTemplate")
class Machine {
    final int MACHINE_FORWARD = 1;
    final int MACHINE_BACKWARD = 4;
    final int MACHINE_LEFT = 3;
    final int MACHINE_RIGHT = 2;
    final int MACHINE_STOP = 0;

    private final Genuino101 genuino = new Genuino101();
    private final BLE ble = genuino.getBLE();

    private BluetoothGattCharacteristic mCharacteristicMotorDirection;
    private BluetoothGattCharacteristic mCharacteristicMotorLeftSpeed;
    private BluetoothGattCharacteristic mCharacteristicMotorRightSpeed;
    private BluetoothGattCharacteristic mCharacteristicMachineState;

    private int speedMain = 0, speedOffsetLeft, speedOffsetRight;
    private int machineState;

    Machine() {

    }

    Genuino101 getControlBoard() {
        return this.genuino;
    }
    int getRunSpeed() {
        return this.speedMain;
    }
    int getMachineState() {
        return this.machineState;
    }

    void setRunSpeed(int speed) {
        this.speedMain = speed;
    }
    void setSpeedOffsetLeft(int speed) {
        this.speedOffsetLeft = speed;
    }
    void setSpeedOffsetRight(int speed) {
        this.speedOffsetRight = speed;
    }
    void setMachineState(int state) {
        this.machineState = state;
    }

    void transferMovingOperation(int direction) {
        int speedLeft = speedMain + speedOffsetLeft;
        int speedRight = speedMain + speedOffsetRight;

        if(mCharacteristicMotorLeftSpeed != null) ble.writeCharacteristic(mCharacteristicMotorLeftSpeed, speedLeft);
        if(mCharacteristicMotorLeftSpeed != null) ble.writeCharacteristic(mCharacteristicMotorRightSpeed, speedRight);
        if(mCharacteristicMotorDirection != null) ble.writeCharacteristic(mCharacteristicMotorDirection, direction);
    }
    void commConnect() {
        ble.connect();
    }
    void commDisconnect() {
        ble.disconnect();
    }
    void commClose() {
        ble.close();
    }
    void bindBLEService(Context context) {
        ble.bindService(context);
    }
    void getGattServices() {
        getGattServices(ble.getSupportedGattServices());
    }

    void readMachineState() {
        ble.readCharacteristic(mCharacteristicMachineState);
    }
    IntentFilter makeGattUpdateIntentFilter() {
        return ble.makeGattUpdateIntentFilter();
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void getGattServices(List<BluetoothGattService> gattServices) {
//        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
//        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

        String uuid;

        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
//            String LIST_NAME = "NAME";
//            String LIST_UUID = "UUID";

//            HashMap<String, String> currentServiceData = new HashMap<>();

//            uuid = gattService.getUuid().toString();

//            currentServiceData.put(LIST_NAME, gattAttributes.lookup(uuid, "Unknown service"));
//            currentServiceData.put(LIST_UUID, uuid);
//            gattServiceData.add(currentServiceData);

//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
//            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                charas.add(gattCharacteristic);
//                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                switch (uuid) {
                    case gattAttributes.UUID_MOTOR_DIRECTION:
                        mCharacteristicMotorDirection = gattCharacteristic;
                        break;
                    case gattAttributes.UUID_MOTOR_LEFT_SPEED:
                        mCharacteristicMotorLeftSpeed = gattCharacteristic;
                        break;
                    case gattAttributes.UUID_MOTOR_RIGHT_SPEED:
                        mCharacteristicMotorRightSpeed = gattCharacteristic;
                        break;
                    case gattAttributes.UUID_MACHINE_STATE:
                        mCharacteristicMachineState = gattCharacteristic;
                        break;
                }

//                currentCharaData.put(LIST_NAME, gattAttributes.lookup(uuid, "Unknown characteristic"));
//                currentCharaData.put(LIST_UUID, uuid);
//                gattCharacteristicGroupData.add(currentCharaData);
            }

//            mGattCharacteristics.add(charas);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
}
