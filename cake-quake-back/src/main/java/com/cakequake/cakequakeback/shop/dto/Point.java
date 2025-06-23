package com.cakequake.cakequakeback.shop.dto;

// 주소 -> 좌표 변환 결과를 담는 객체
public class Point {
    private final double latitude;
    private final double longitude;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
