package com.example.mapthreadsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.regionlibrary.Region;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    // Elementos visuais
    private TextView latTextView;
    private TextView lngTextView;

    private final int FINE_LOCATION_PERMISSION = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private MapHandler mapHandler;


    private RegionService regionService;


    private Thread threadCurrentLocation = new Thread(() -> {

        // A cada 2 segunos atualiza a localização
        while (true) {
            try {
                getLastLocation();
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        // relaciona a variavel com o objeto
        latTextView = findViewById(R.id.latTextView);
        lngTextView = findViewById(R.id.lngTextView);

        regionService = new RegionService(this::callbackToast);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapHandler = new MapHandler(regionService);

        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapHandler.setMap(googleMap);
                threadCurrentLocation.start();
            }
        });
    }

    private void callbackToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    public void addRegion(View view) {
        Location currentLocation = mapHandler.getLastLocation();
        if(currentLocation == null) return;

        Region newRegion = new Region(currentLocation.getLatitude(), currentLocation.getLongitude());
        regionService.addRegion(newRegion);
    }

    public void saveDatabase(View view) {
        regionService.saveDatabase();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
            return;
        }
        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mapHandler.updateLocation(location);
                    runOnUiThread(() -> {
                        latTextView.setText("Latitude: " + location.getLatitude());
                        lngTextView.setText("Longitude: " + location.getLongitude());
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }
}