package com.example.water.cproject;

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
}
