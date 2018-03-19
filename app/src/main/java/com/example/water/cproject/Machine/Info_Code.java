package com.example.water.cproject.Machine;

/**
 * Created by watering on 18. 3. 16.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Info_Code {
    private int id;
    private String code;
    private String date;

    public int getId() {
        return id;
    }
    public String getDate() {
        return date;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
