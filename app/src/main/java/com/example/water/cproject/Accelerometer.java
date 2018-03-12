package com.example.water.cproject;

/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Accelerometer {
    private float x, y, z;

    public Accelerometer() {
        updateData(0,0,0);
    }

    public void updateData(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Accelerometer getData() {
        return this;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }
}
