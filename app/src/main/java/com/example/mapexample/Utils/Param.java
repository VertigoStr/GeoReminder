package com.example.mapexample.Utils;

public class Param {
    private int id;
    private String value;

    public Param(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId(){
        return id;
    }

    public void setId(int value) {
        this.id = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
