package com.example.meditime;

public class DataModel {
    private String medicine;
    private String date;
    private String time;

    public DataModel(String medicine, String date, String time) {
        this.medicine = medicine;
        this.date = date;
        this.time = time;
    }

    public String getMedicine() {
        return medicine;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
