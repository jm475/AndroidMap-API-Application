package com.example.assignmentthree;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.assignmentthree.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the navigation bar by enabling Immersive Sticky mode
        int uiOptionsSticky = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptionsSticky);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Check to see if permissions are granted
        checkPermissions();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        checkPermissions();
    }


    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions have not yet been granted
            // Launch a location permission request
            askForPermission();
        } else {
            // Permissions have already been granted
            Toast.makeText(this, "Permission status: already granted", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
        }

    }



    public void askForPermission() {
        // Create a location permission request
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            // Check if permission has now been granted
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                Toast.makeText(this, "Permission status: access granted", Toast.LENGTH_SHORT).show();
                                getCurrentLocation();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                Toast.makeText(this, "Permission status: fine not granted", Toast.LENGTH_SHORT).show();
                            } else {
                                // No location access granted.
                                Toast.makeText(this, "Permission status: no permissions granted", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

        // Launch the location permission request
        locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

    }


    public void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY,null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                        // Logic to handle location object
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_SHORT).show();


                            //Add a marker at last known location and move the camera
                            LatLng lastLocation = new LatLng(lat, lon);
                            mMap.addMarker(new MarkerOptions().position(lastLocation).title("Marker Test"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 11));
                        }
                        else {
                        // Handle a location not being found
                            Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_SHORT).show();
                        }
            }
        });






//        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. This can sometimes be null.
//                        if (location != null) {
//                        // Logic to handle location object
//                            double lat = location.getLatitude();
//                            double lon = location.getLongitude();
//                            Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_SHORT).show();
//
//
//                            //Add a marker at last known location and move the camera
//                            LatLng lastLocation = new LatLng(lat, lon);
//                            mMap.addMarker(new MarkerOptions().position(lastLocation).title("Marker Test"));
//                            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));
//                        }
//                        else {
//                        // Handle a location not being found
//                            Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
      }


    
}



