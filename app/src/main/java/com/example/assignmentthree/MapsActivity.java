package com.example.assignmentthree;

import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.assignmentthree.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.JsonObject;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    FusedLocationProviderClient fusedLocationClient;
    double lat;
    double lon;
    LatLng placeLatLng;
    private RequestQueue queue;
    private String weatherIcon;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Hide the navigation bar by enabling Immersive Sticky mode
        int uiOptionsSticky = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptionsSticky);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialise our places with our API key
        Places.initialize(getApplicationContext(), "AIzaSyA5pUxD_2Xi1s-bga4itPVaq-VblEHmxg8");

        //Create a new placesClient
        PlacesClient placesClient = Places.createClient(this);

        //Get our autocompletefragment search view
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Set the country of our autocomplete to only display places in New Zealand
        autocompleteFragment.setCountries("NZ");

        //Make sure our autocomplete returns only names and lat/lng
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));


        queue = Volley.newRequestQueue(this);

        //Listener to see if a location has been clicked
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                lat = place.getLatLng().latitude;
                lon = place.getLatLng().longitude;
                placeLatLng = place.getLatLng();
                getWeatherPin();
                getCameraPin();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getWeatherPin(){
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=1a06e7f638260a986797aff2e5bb52af";
        // Request a object response from the provided URL.
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //Get a JSONArray from response object
                    JSONArray jsonArray = response.getJSONArray("weather");
//                    for(int i = 0; i < jsonArray.length(); i++){
//                        JSONObject weatherArray = jsonArray.getJSONObject(i);
//                        String weatherValue = weatherArray.getString("main").toLowerCase(Locale.ROOT);
//                    }
                    //Get the object that is at index 0 in our JSONArray
                    JSONObject weatherArray = jsonArray.getJSONObject(0);

                    //Get the resource ID to be used
                    int test = getResources().getIdentifier(weatherArray.getString("main").toLowerCase(Locale.ROOT), "drawable", getPackageName());
                    mMap.addMarker(new MarkerOptions()
                            .position(placeLatLng)
                            .title("Weather")
                            .icon(BitmapDescriptorFactory.fromResource(test)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 11));
                } catch (JSONException e) {
                    Log.d("error", e.toString());
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("volleyError","Error :" + error.getMessage());
            }
        });
        queue.add(objectRequest);
    }

    public void getCameraPin(){
        //REMEMBER USE &KEY
        String urlImages = "https://api.windy.com/api/webcams/v2/list/limit=5/nearby=-37,175,100?show=webcams:image&key=qkz8BHlYJaxtU9sZfmLNSo2sJr8y3r6R";

        String urlLocation = "https://api.windy.com/api/webcams/v2/list/limit=5/nearby=" + lat + "," + lon + ",100" + "?show=webcams:image,location&key=qkz8BHlYJaxtU9sZfmLNSo2sJr8y3r6R";

        // USE THIS WEBSITE TO COMPARE https://www.windy.com/?-48.225,-174.023,4,m:cCqakXS
        //ORIGINAL URLLOCATION https://api.windy.com/api/webcams/v2/list/limit=5/nearby=-37.792,175.174,100?show=webcams:location&key=qkz8BHlYJaxtU9sZfmLNSo2sJr8y3r6R
        // Request a object response from the provided URL.
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, urlLocation, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //Get a JSONObject from response object
                    JSONObject jsonObject = response.getJSONObject("result");

                    //Get a JSONarray from the JSONObject
                    JSONArray jsonArray = jsonObject.getJSONArray("webcams");


                    for(int i = 0; i < jsonArray.length(); i++){

                        //Get the JSONObject at index i
                        JSONObject weatherObject = jsonArray.getJSONObject(i);

                        //Get the location details for the
                        JSONObject weatherObject2 = weatherObject.getJSONObject("location");

                        Log.d("response", weatherObject.getString("title"));

                        //Get the resource ID to be used
                        int test = getResources().getIdentifier("camera", "drawable", getPackageName());
                        //Get the latitude and longitude to be used
                        double lat2 = weatherObject2.getDouble("latitude");
                        double lon2 = weatherObject2.getDouble("longitude");
                        LatLng latLng2 = new LatLng(lat2,lon2);

                        //Add a marker to the maps
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng2)
                                .title("Camera " + weatherObject.getString("title"))
                                .icon(BitmapDescriptorFactory.fromResource(test)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 11));

                    }


                } catch (JSONException e) {
                    Log.d("error", e.toString());
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("volleyError","Error :" + error.getMessage());
            }
        });
        queue.add(objectRequest);

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
        // Launch the location permission request
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

    }


    public void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            //fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    // Logic to handle location object
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_SHORT).show();

                    //Add a marker at current location and move the camera
                    LatLng currentLocation = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker Test"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11));

                } else {
                    // Handle a location not being found
                    Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}



