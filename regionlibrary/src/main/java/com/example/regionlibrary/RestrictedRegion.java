package com.example.regionlibrary;

import com.example.regionlibrary.LocationUtils;
import com.example.regionlibrary.Region;

public class RestrictedRegion extends Region {
    private Region mainRegion;
    private Boolean restricted = false;


    public RestrictedRegion() {
        super();
        restricted = true;
    }

    public RestrictedRegion(double latitude, double longitude, Region mainRegion) {
        super(latitude, longitude);
        this.name = "Restricted " + this.name;
        this.mainRegion = mainRegion;
        this.type = "RestrictedRegion";
    }

    public String getMainRegionId() {
        return mainRegion.getId();
    }
    @Override
    public Boolean isNear(Region region) {
        // distancia minima de 50m apenas para fim de facilitar a demonstracao
        final int min_distance = 50;
        return distance(region) < min_distance;
    }
}
