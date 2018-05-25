package com.example.water.cproject.machine;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.IntentFilter;

import com.example.water.cproject.ble.BLE;
import com.example.water.cproject.ble.gattAttributes;
import com.example.water.cproject.genuino.Genuino101;

import java.util.List;
import java.util.Locale;

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
    private BluetoothGattCharacteristic mCharacteristicMotorSpeed;
    private BluetoothGattCharacteristic mCharacteristicOperateMode;
    private BluetoothGattCharacteristic mCharacteristicOperatePIDGain;
    private BluetoothGattCharacteristic mCharacteristicStateMachine;

    private int speedMain = 0, speedOffsetLeft = 100, speedOffsetRight = 100;
    private int state, mode;
    private int[] pid = {0, 0, 0};

    public Machine() {

    }

    public Genuino101 getControlBoard() {
        return this.genuino;
    }
    public int getRunSpeed() {
        return this.speedMain;
    }
    public int getState() {
        return this.state;
    }
    public int getMode() {
        return this.mode;
    }
    public int[] getPID() {
        return this.pid;
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
    public void setState(int state) {
        this.state = state;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    public void setPID(int p, int i, int d) {
        this.pid[0] = p;
        this.pid[1] = i;
        this.pid[2] = d;
    }

    public void sendMode(int mode) {
        if(mCharacteristicOperateMode != null) ble.writeCharacteristic(mCharacteristicOperateMode, mode);
    }
    public void sendSpeed() {
        int speedLeft = speedMain * (speedOffsetLeft / 100);
        int speedRight = speedMain * (speedOffsetRight / 100);

        String speed = String.format(Locale.getDefault(),"%03d,%03d",speedLeft,speedRight);

        if(mCharacteristicMotorSpeed != null) ble.writeCharacteristic(mCharacteristicMotorSpeed, speed.getBytes());
    }
    public void sendPID() {
        String strPID = String.format(Locale.getDefault(),"%03d,%03d,%03d",pid[0],pid[1],pid[2]);
        if(mCharacteristicOperatePIDGain != null) ble.writeCharacteristic(mCharacteristicOperatePIDGain, strPID.getBytes());
    }

    public void operate(int operation) {
        if(mCharacteristicMotorDirection != null) ble.writeCharacteristic(mCharacteristicMotorDirection, operation);
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
        ble.readCharacteristic(mCharacteristicStateMachine);
    }
    public IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = ble.makeGattUpdateIntentFilter();
        intentFilter.addAction(MachineService.ACTION);
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
                    case gattAttributes.UUID_MOTOR_SPEED:
                        mCharacteristicMotorSpeed = gattCharacteristic;
                        break;
                    case gattAttributes.UUID_OPERATE_PID:
                        mCharacteristicOperatePIDGain = gattCharacteristic;
                        break;
                    case gattAttributes.UUID_OPERATE_MODE:
                        mCharacteristicOperateMode = gattCharacteristic;
                        break;
                    case gattAttributes.UUID_STATE_MACHINE:
                        mCharacteristicStateMachine = gattCharacteristic;
                        break;
                }
            }
        }
    }
}
