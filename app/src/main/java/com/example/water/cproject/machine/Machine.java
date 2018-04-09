package com.example.water.cproject.machine;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.IntentFilter;

import com.example.water.cproject.ble.BLE;
import com.example.water.cproject.ble.gattAttributes;
import com.example.water.cproject.genuino.Genuino101;

import java.util.List;

/**
 * Created by water on 2017-04-19.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Machine {
    public final int MOTOR_FORWARD = 2;
    public final int MOTOR_BACKWARD = 5;
    public final int MOTOR_LEFT = 4;
    public final int MOTOR_RIGHT = 3;
    public final int MOTOR_STOP = 0;
    public final int MOTOR_RUN = 1;
    public final int IS_AUTO = 1;
    public final int IS_MANUAL = 0;

    private final Genuino101 genuino = new Genuino101();
    private final BLE ble = genuino.getBLE();

    private BluetoothGattCharacteristic mCharacteristicMotorDirection;
    private BluetoothGattCharacteristic mCharacteristicMotorLeftSpeed;
    private BluetoothGattCharacteristic mCharacteristicMotorRightSpeed;
    private BluetoothGattCharacteristic mCharacteristicIsAuto;
    private BluetoothGattCharacteristic mCharacteristicMachineState;

    private int speedMain = 0, speedOffsetLeft, speedOffsetRight;
    private int motorState;

    public Machine() {

    }

    public Genuino101 getControlBoard() {
        return this.genuino;
    }
    public int getRunSpeed() {
        return this.speedMain;
    }
    public int getMotorState() {
        return this.motorState;
    }

    public void setRunSpeed(int speed) {
        this.speedMain = speed;
    }
    public void setSpeedOffsetLeft(int speed) {
        this.speedOffsetLeft = speed;
    }
    public void setSpeedOffsetRight(int speed) {
        this.speedOffsetRight = speed;
    }
    public void setMotorState(int state) {
        this.motorState = state;
    }

    public void operate(int operation) {
        int speedLeft = speedMain + speedOffsetLeft;
        int speedRight = speedMain + speedOffsetRight;

        if(mCharacteristicMotorLeftSpeed != null) ble.writeCharacteristic(mCharacteristicMotorLeftSpeed, speedLeft);
        if(mCharacteristicMotorLeftSpeed != null) ble.writeCharacteristic(mCharacteristicMotorRightSpeed, speedRight);
        if(mCharacteristicMotorDirection != null) ble.writeCharacteristic(mCharacteristicMotorDirection, operation);
        if(mCharacteristicIsAuto != null) ble.writeCharacteristic(mCharacteristicIsAuto, operation);
    }
    public void commConnect() {
        ble.connect();
    }
    public void commDisconnect() {
        ble.disconnect();
    }
    public void commClose() {
        ble.close();
    }
    public void bindService(Context context) {
        ble.bindService(context);
        genuino.bindService(context);
    }
    public void getGattServices() {
        getGattServices(ble.getSupportedGattServices());
    }

    public void readMachineState() {
        ble.readCharacteristic(mCharacteristicMachineState);
    }
    public IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = ble.makeGattUpdateIntentFilter();
        intentFilter.addAction(IMUService.ACTION);
        return intentFilter;
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the imu structure that is bound to the ExpandableListView
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
                    case gattAttributes.UUID_IS_AUTO:
                        mCharacteristicIsAuto = gattCharacteristic;
                        break;
                    case gattAttributes.UUID_MACHINE_STATE:
                        mCharacteristicMachineState = gattCharacteristic;
                        break;
                }
            }
        }
    }
}
