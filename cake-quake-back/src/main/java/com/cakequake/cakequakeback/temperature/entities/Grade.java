package com.cakequake.cakequakeback.temperature.entities;

public enum Grade {

    FROZEN,
    BASIC,
    VIP,
    VVIP;

    public static Grade fromTemperature(double temperature) {
        if (temperature < 36.5) {
            return FROZEN;
        } else if (temperature < 60) {
            return  BASIC;
        } else if (temperature < 90) {
            return VIP;
        } else {
            return VVIP;
        }
    }

}
