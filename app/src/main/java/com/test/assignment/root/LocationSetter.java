package com.test.assignment.root;

/**
 * Created by shadaf on 1/7/18.
 * This is a getter setter class to store geo coordinates.
 */

public class LocationSetter {

    private double latitude = 0, longitude = 0;

    public LocationSetter(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
}
