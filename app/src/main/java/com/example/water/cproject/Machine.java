package com.example.water.cproject;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.IntentFilter;

import java.util.List;

import static com.example.water.cproject.IMUService.ACTION;

/**
 * Created by water on 2017-04-19.
 */

@SuppressWarnings("DefaultFileTemplate")
class Machine {
    final int MOTOR_FORWARD = 1;
    final int MOTOR_BACKWARD = 4;
    final int MOTOR_LEFT = 3;
    final int MOTOR_RIGHT = 2;
    final int MOTOR_STOP = 0;

    private final Genuino101 genuino = new Genuino101();
    private final BLE ble = genuino.getBLE();

    private BluetoothGattCharacteristic mCharacteristicMotorDirection;
    private BluetoothGattCharacteristic mCharacteristicMotorLeftSpeed;
    private BluetoothGattCharacteristic mCharacteristicMotorRightSpeed;
    private BluetoothGattCharacteristic mCharacteristicMachineState;

    private int speedMain = 0, speedOffsetLeft, speedOffsetRight;
    private int motorState;

    Machine() {

    }

    Genuino101 getControlBoard() {
        return this.genuino;
    }
    int getRunSpeed() {
        return this.speedMain;
    }
    int getMotorState() {
        return this.motorState;
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
    void setMotorState(int state) {
        this.motorState = state;
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
    void bindService(Context context) {
        ble.bindService(context);
        genuino.bindService(context);
    }
    void getGattServices() {
        getGattServices(ble.getSupportedGattServices());
    }

    void readMachineState() {
        ble.readCharacteristic(mCharacteristicMachineState);
    }
    IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = ble.makeGattUpdateIntentFilter();
        intentFilter.addAction(ACTION);
        return intentFilter;
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void getGattServices(List<BluetoothGattService> gattServices) {
        String uuid;

        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
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
            }
        }
    }
}
