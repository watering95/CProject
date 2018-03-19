package com.example.water.cproject.Machine;

/**
 * Created by watering on 18. 3. 16.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Info_Machine {
    private int code;
    private String time;
    private int state;
    private float[] imu = new float[6];

    public int getCode() {
        return code;
    }
    public String getTime() {
        return time;
    }
    public int getState() {
        return state;
    }
    public float[] getImu() {
        return imu;
    }

    public void setCode(int code) {
        this.code = code;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setState(int state) {
        this.state = state;
    }
    public void setImu(float[] imu) {
        this.imu = imu;
    }
}
