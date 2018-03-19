package com.example.water.cproject.Genuino;

/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Gyroscope {
    private float x, y, z;

    public Gyroscope() {
        updateData(0,0,0);
    }

    public void updateData(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Gyroscope getData() {
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
