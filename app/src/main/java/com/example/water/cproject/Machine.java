package com.example.water.cproject;

import javax.crypto.Mac;

/**
 * Created by water on 2017-04-19.
 */

public class Machine {
    public int MACHINE_FORWARD = 1;
    public int MACHINE_BACKWARD = 4;
    public int MACHINE_LEFT = 3;
    public int MACHINE_RIGHT = 2;
    public int MACHINE_STOP = 0;

    private int mRunSpeed;
    private int mDirection;
    public Genuino101 mGenuino;

    public Machine() {
        mRunSpeed = 0;
        mDirection = MACHINE_STOP;
        mGenuino = new Genuino101();
    }
    public void setRunSpeed(int speed) {
        mRunSpeed = speed;
    }
    public int getRunSpeed() {
        return mRunSpeed;
    }
    public void setDirection(int direc) {
        mDirection = direc;
    }
    public int getDirection() {
        return mDirection;
    }
}
