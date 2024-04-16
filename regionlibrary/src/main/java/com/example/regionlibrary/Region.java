package com.example.regionlibrary;

import static java.lang.System.nanoTime;

import com.google.android.gms.maps.model.LatLng;

public class Region {
    protected String name;
    protected double latitude;
    protected double longitude;
    protected String id;
    protected long timestamp;
    protected String type;

    static private int counter = 1;

    public Region() {}

    public Region(double latitude, double longitude) {
        this.name = "Regiao - " + counter++;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = java.util.UUID.randomUUID().toString();
        this.timestamp = nanoTime();
        this.type = "Region";
    }

    public String getType() { return type; }
    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double distance(Region region) {
        return LocationUtils.Distance(this.latitude, this.longitude, region.getLatitude(), region.getLongitude());
    }

    public Boolean isNear(Region region) {
        // distancia minima de 200m apenas para fim de facilitar a demonstracao
        final int min_distance = 200;
        return distance(region) < min_distance;
    }

}
