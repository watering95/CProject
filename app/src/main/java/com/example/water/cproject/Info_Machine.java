package com.example.water.cproject;

/**
 * Created by watering on 18. 3. 16.
 */

class Info_Machine {
    private String code;
    private String date;
    private String time;
    private int state;
    private float[] imu = new float[6];

    public String getCode() {
        return code;
    }
    public String getDate() {
        return date;
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

    public void setCode(String code) {
        this.code = code;
    }
    public void setDate(String date) {
        this.date = date;
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
