package com.example.water.cproject;

/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
class Genuino101 {
    private final BLE mBLE = new BLE();
    private final Gyroscope mGyro = new Gyroscope();
    private final Accelerometer mAccelerometer = new Accelerometer();
    private float positionX, positionY, positionZ;
    private float speedX, speedY, speedZ;

    public Genuino101() {
        initPosition();
    }

    public Gyroscope getGyroscope() {
        return mGyro;
    }

    public Accelerometer getAccelerometer() {
        return mAccelerometer;
    }

    public BLE getBLE() {
        return mBLE;
    }

    private void initPosition() {
        setPotision(0,0,0);
        setSpeed(0,0,0);
    }

    private void setPotision(float x, float y, float z) {
        positionX = x;
        positionY = y;
        positionZ = z;
    }
    public float getPositionX() {
        return positionX;
    }
    public float getPositionY() {
        return positionY;
    }
    public float getPositionZ() {
        return positionZ;
    }

    private void setSpeed(float x, float y, float z) {
        speedX = x;
        speedY = y;
        speedZ = z;
    }
    public float getSpeedX() {
        return speedX;
    }
    public float getSpeedY() {
        return speedY;
    }
    public float getSpeedZ() {
        return speedZ;
    }
}
