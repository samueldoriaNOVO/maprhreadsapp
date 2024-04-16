package com.example.mapthreadsapp;

import com.example.regionlibrary.Region;
import com.example.regionlibrary.RestrictedRegion;
import com.example.regionlibrary.SubRegion;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapHandler {
    private GoogleMap mMap;
    private Boolean firstLocation = true;
    private final RegionService regionService;
    private android.location.Location currentLocation = null;

    public MapHandler(RegionService regionService) {
        this.regionService = regionService;
    }

    public void updateLocation(android.location.Location location) {
        this.currentLocation = location;
        LatLng currentLatLng = new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude());

        // create a blue circle to represent the current location
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // add all the regions to the map
        for (Region region : regionService.getAllRegions()) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(region.getLatLng())
                    .title(region.getName());

            if(region instanceof SubRegion)
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            else if(region instanceof RestrictedRegion)
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            else
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mMap.addMarker(markerOptions);
        }

        // Da um zoom pra perto do primeiro local reconhecido no mapa
        if(firstLocation) {
            firstLocation = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
        }
    }

    public android.location.Location getLastLocation() {
        return this.currentLocation;
    }

    public void setMap(GoogleMap map) {
        this.mMap = map;
    }
}
