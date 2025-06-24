package com.cakequake.cakequakeback.shop.dto;

// 주소 -> 좌표 변환 결과를 담는 객체
public class Point {
    private final double lat;
    private final double lon;

    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLatitude() { return lat; }
    public double getLongitude() { return lon; }
}
