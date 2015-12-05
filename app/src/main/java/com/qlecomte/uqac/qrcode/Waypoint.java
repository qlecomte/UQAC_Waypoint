package com.qlecomte.uqac.qrcode;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Waypoint {
    private String name;
    private double latitude;
    private double longitude;
    private float icon;

    public Waypoint() {
        this.name = "";
        this.latitude = 0;
        this.longitude = 0;
        this.icon = BitmapDescriptorFactory.HUE_CYAN;
    }

    public Waypoint(String name) {
        this.name = name;
        this.latitude = 0;
        this.longitude = 0;
        this.icon = BitmapDescriptorFactory.HUE_CYAN;
    }

    public Waypoint(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = BitmapDescriptorFactory.HUE_CYAN;
    }

    public Waypoint(String name, double latitude, double longitude, float icon) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getIcon() {
        return icon;
    }
    public void setIcon(float icon) {
        this.icon = icon;
    }

}
