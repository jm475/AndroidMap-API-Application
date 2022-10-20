package com.example.assignmentthree;

import static org.junit.Assert.*;

import androidx.annotation.NonNull;

import com.google.android.datatransport.runtime.scheduling.Scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class MapsActivityTest {
    private Object RxAndroidPlugins;

    //MapsActivity mapsActivity;


    @Before
    public void setUp() throws Exception {
        //mapsActivity = new MapsActivity();
    }

    @After
    public void tearDown() throws Exception {
       // mapsActivity = null;
    }



    @Test
    public void locationPermissionsNotGranted(){

    }

    @Test
    public void locationPermissionsGranted(){

    }

    @Test
    public void weatherPinValidInput(){
        MapsActivity mapsActivity = new MapsActivity();
        mapsActivity.lat = 37;
        mapsActivity.lon = 175;
        mapsActivity.getCameraPin();
        //assertTrue();

    }

    @Test
    public void weatherPinInvalidInput(){

    }

    @Test
    public void cameraPinValidInput(){

    }

    @Test
    public void cameraPinInvalidInput(){

    }

    @Test
    public void weatherMarkerClicked(){

    }

    @Test
    public void cameraMarkerClicked(){

    }





}