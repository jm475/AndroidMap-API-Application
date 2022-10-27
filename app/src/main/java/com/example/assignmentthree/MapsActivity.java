package com.example.assignmentthree;

import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;



 /**
  * MapsActivity class that displays an interactive map and a search bar.
  * also used as default activity when app is first launched
  */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Launcher to ask for location permissions
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

    //Initialise global variables
    GoogleMap mMap;
    private ActivityMapsBinding binding;
    FusedLocationProviderClient fusedLocationClient;
    double lat;
    double lon;
    LatLng placeLatLng;
    private RequestQueue queue;
    int markerCount;


    public int getMarkerCount(){
        return markerCount;
    }


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

        //Instantiate the RequestQueue
        queue = Volley.newRequestQueue(this);


        //Listener to see if a location has been clicked.
        //Once location has been clicked, calls methods to add pins and move the camera to the selected location
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                markerCount = 0;
                mMap.clear();
                lat = place.getLatLng().latitude;
                lon = place.getLatLng().longitude;
                placeLatLng = place.getLatLng();
                getWeatherPin();
                getCameraPin();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 11));
            }

            @Override
            public void onError(Status status) {

            }
        });
    }


     /**
      * Method that creates a JSONObject request given a url with latitude and longitude coordinates
      * Retrieves data surrounding weather information and uses the data to add markers onto the map
      */
    private void getWeatherPin(){
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=1a06e7f638260a986797aff2e5bb52af";
        // Request a object response from the provided URL.
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //Get a JSONArray from response object
                    JSONArray jsonArray = response.getJSONArray("weather");

                    //Get the object that is at index 0 in our JSONArray
                    JSONObject weatherArray = jsonArray.getJSONObject(0);

                    //Get the resource ID to be used
                    int icon = getResources().getIdentifier(weatherArray.getString("main").toLowerCase(Locale.ROOT), "drawable", getPackageName());

                    //Add a marker to the map with data
                    mMap.addMarker(new MarkerOptions()
                            .position(placeLatLng)
                            .title(weatherArray.getString("main"))
                            .snippet(weatherArray.getString("description"))
                            .icon(BitmapDescriptorFactory.fromResource(icon)));
                    markerCount++;

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
      * Method that creates a JSONObject request given a url with latitude and longitude coordinates
      * Retrieves data surrounding web cameras and uses the data to add markers onto the map
      */
    public void getCameraPin(){
        String url = "https://api.windy.com/api/webcams/v2/list/limit=5/nearby=" + lat + "," + lon + ",100" + "?show=webcams:image,location&key=qkz8BHlYJaxtU9sZfmLNSo2sJr8y3r6R";

        //Request a object response from the provided URL.
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //Get a JSONObject from response object
                    JSONObject jsonObject = response.getJSONObject("result");

                    //Get a JSONarray from the JSONObject
                    JSONArray jsonArray = jsonObject.getJSONArray("webcams");

                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject JSONWebcams = jsonArray.getJSONObject(i);

                        //Split the array to get location and image details
                        JSONObject jsonLocation = JSONWebcams.getJSONObject("location");
                        JSONObject jsonImage = JSONWebcams.getJSONObject("image");
                        JSONObject jsonCurrent = jsonImage.getJSONObject("current");

                        //Get the resource ID icon to be used
                        int icon = getResources().getIdentifier("camera", "drawable", getPackageName());

                        //Get the latitude and longitude to be used
                        double latCamera = jsonLocation.getDouble("latitude");
                        double lonCamera = jsonLocation.getDouble("longitude");
                        LatLng latLngCamera = new LatLng(latCamera,lonCamera);

                        //Get the location string
                        String location = jsonLocation.getString("city") + ", " + jsonLocation.getString("region");

                        //Add a marker to the map with data
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLngCamera)
                                .title(JSONWebcams.getString("title"))
                                .snippet(location)
                                .icon(BitmapDescriptorFactory.fromResource(icon)));
                        markerCount++;

                        //Add the image to the marker
                        marker.setTag(jsonCurrent.getString("thumbnail"));
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
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Check for permissions
        checkPermissions();

        //Listener to see if a marker has been clicked.
        //Once a marker has been clicked, checks to see if it is a Camera Pin.
        //Then starts a new intent if marker is a Camera Pin
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                //If the marker clicked is a Camera Pin
                if(marker.getTag() != null){
                    //Create intent and add extra data
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra("markerTitle", marker.getTitle());
                    intent.putExtra("markerSnippet", marker.getSnippet());
                    intent.putExtra("markerImage", (String) marker.getTag());
                    //Start the intent
                    startActivity(intent);
                }
                return false;
            }
        });
    }

     /**
      * Method that checks if location permissions have been granted.
      */
    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permissions have not yet been granted
            //Launch a location permission request
            askForPermission();
        } else {
            //Permissions have already been granted
            //Toast.makeText(this, "Permission status: already granted", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
        }

    }


     /**
      * Method that launches a locationPermissionRequest
      */
    public void askForPermission() {
        // Create a location permission request
        // Launch the location permission request
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }


     /**
      * Method that uses a FusedLocationProviderClient to get the current location of the device
      * to display on the map.
      * Also retrieves the name of the current location
      */
    public void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Logic to handle location object
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();


                    //Get the locality name of the location
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(lat, lon, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address obj = addresses.get(0);
                    String add = obj.getLocality();




                    //Add a marker at current location and move the camera
                    LatLng currentLocation = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title(add));
                    markerCount++;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11));

                } else {
                    //Handle a location not being found
                    Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}



