package com.example.water.cproject;

/**
 * Created by water on 2017-04-18.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Gyroscope {
    private float mGx, mGy, mGz;

    public Gyroscope() {
        updateData(0,0,0);
    }

    private void updateData(float x, float y, float z) {
        mGx = x;
        mGy = y;
        mGz = z;
    }
    public Gyroscope getData() {
        return this;
    }
    public float getGx() {
        return mGx;
    }
    public float getGy() {
        return mGy;
    }
    public float getGz() {
        return mGz;
    }
}
