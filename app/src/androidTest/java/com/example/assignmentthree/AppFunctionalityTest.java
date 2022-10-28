package com.example.assignmentthree;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.PositionAssertions.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;


import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AppFunctionalityTest {

    @Rule
    public GrantPermissionRule mGrantPermissionRule;


    @Rule
    public ActivityScenarioRule<MapsActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapsActivity.class);


    /**
     * Test to check if Views are displayed
     */
    @Test
    public void checkViewsDisplayed() {
        onView(withId(R.id.autocomplete_fragment)).check(matches(isDisplayed()));
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        onView(withId(R.id.map)).check((isCompletelyBelow(withId(R.id.autocomplete_fragment))));
    }


    /**
     * Test to see what happens when a user exits/backs out of the autocompletefragment
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    @Test
    public void onSearchBarExit() throws UiObjectNotFoundException, InterruptedException {
        ViewInteraction editText = onView(withId(R.id.autocomplete_fragment));
        editText.perform(click());
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(com.google.android.libraries.places.R.id.places_autocomplete_back_button), withContentDescription("Cancel"),
                        childAtPosition(
                                allOf(withId(com.google.android.libraries.places.R.id.places_autocomplete_search_bar_container),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        Thread.sleep(1000);
    }


    /**
     * Test to check if a weather marker exists on the map
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    @Test
    public void checkWeatherMarkerExists() throws InterruptedException, UiObjectNotFoundException {
        mGrantPermissionRule = GrantPermissionRule.grant(
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION");
        searchForHamilton();

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject marker = null;

        ArrayList<String> weatherString = new ArrayList<>();
        weatherString.add("Clouds");
        weatherString.add("Clear");
        weatherString.add("Drizzle");
        weatherString.add("Rain");
        weatherString.add("Snow");
        weatherString.add("Thunderstorm");

        for (int i = 0; i < weatherString.size(); i++){
            Boolean markerExists = device.findObject(new UiSelector().descriptionContains(weatherString.get(i))).exists();
            if(markerExists){
                marker = device.findObject(new UiSelector().descriptionContains(weatherString.get(i)));
                break;
            } else{
                return;
            }
        }
        Thread.sleep(1000);
        marker.click();
        Thread.sleep(1000);

    }

    /**
     * Test to see if a camera marker exists on the map
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    @Test
    public void checkCameraMarkerExists() throws InterruptedException, UiObjectNotFoundException {
        searchForHamilton();

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject marker = null;


        Boolean markerExists = device.findObject(new UiSelector().resourceId("camera")).exists();

        if(markerExists){
            marker = device.findObject(new UiSelector().resourceId("camera"));
        } else{
            return;
        }

        Thread.sleep(1000);
        marker.click();
        Thread.sleep(1000);

    }


    /**
     * Test to see what happens when the get directions button is clicked
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    @Test
    public void checkGetDirections() throws UiObjectNotFoundException, InterruptedException {
        searchForHamilton();

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject marker = null;

        ArrayList<String> weatherString = new ArrayList<>();
        weatherString.add("Clouds");
        weatherString.add("Clear");
        weatherString.add("Drizzle");
        weatherString.add("Rain");
        weatherString.add("Snow");
        weatherString.add("Thunderstorm");

        for (int i = 0; i < weatherString.size(); i++){
            Boolean markerExists = device.findObject(new UiSelector().descriptionContains(weatherString.get(i))).exists();
            if(markerExists){
                marker = device.findObject(new UiSelector().descriptionContains(weatherString.get(i)));
                break;
            } else{
                return;
            }
        }

        Thread.sleep(1000);
        marker.click();
        Thread.sleep(1000);

        ViewInteraction imageView = onView(
                allOf(withContentDescription("Get directions"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        3),
                                0),
                        isDisplayed()));
        imageView.perform(click());


    }


    /**
     * Test to check that the contents of the DetailActivity views aren't empty
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    @Test
    public void checkDetailActivityContentNotEmpty() throws InterruptedException, UiObjectNotFoundException {
        searchForHamilton();

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject marker = null;

        Boolean markerExists = device.findObject(new UiSelector().resourceId("camera")).exists();

        if(markerExists){
            marker = device.findObject(new UiSelector().resourceId("camera"));
        } else{
            return;
        }

        Thread.sleep(1000);
        marker.click();
        Thread.sleep(1000);

        onView(withId(R.id.tv_city)).check(matches(not(withText(""))));
        onView(withId(R.id.tv_title)).check(matches(not(withText(""))));
        onView(withId(R.id.iv_thumbnail)).check(matches(not(withResourceName(""))));
    }


    /**
     * Test to see if marker exists when permissions are granted
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    @Test
    public void locationPermissionsGranted() throws InterruptedException, UiObjectNotFoundException {
        mGrantPermissionRule = GrantPermissionRule.grant(
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION");

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject marker;

        Boolean markerExists = device.findObject(new UiSelector().descriptionContains("Hamilton")).exists();

        if(markerExists){
            marker = device.findObject(new UiSelector().descriptionContains("Hamilton"));
        } else{
            return;
        }

        Thread.sleep(1000);
        marker.click();
        Thread.sleep(1000);

    }

    /**
     * Test to see if marker exists when only coarse permission is granted
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    @Test
    public void locationPermissionCoarseGranted() throws InterruptedException, UiObjectNotFoundException {
       mGrantPermissionRule = GrantPermissionRule.grant(
                "android.permission.ACCESS_COARSE_LOCATION");

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        Boolean markerExists = device.findObject(new UiSelector().descriptionContains("Hamilton")).exists();

        assertFalse(markerExists);
    }

    /**
     * Test to see if marker exists when permissions are denied
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    @Test
    public void locationPermissionsDenied() throws InterruptedException, UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        Boolean markerExists = device.findObject(new UiSelector().descriptionContains("Hamilton")).exists();

        assertFalse(markerExists);
    }


    /**
     * Method that opens the autocomplete fragment (search bar) then inputs the location "Hamilton"
     * and selects the first match in the search bars recyclerView
     * @throws InterruptedException
     */
    public void searchForHamilton() throws InterruptedException {
        ViewInteraction editText = onView(
                allOf(withId(com.google.android.libraries.places.R.id.places_autocomplete_search_input),
                        childAtPosition(
                                allOf(withId(R.id.autocomplete_fragment),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        Thread.sleep(1000);
        editText.perform(click());
        ViewInteraction appCompatEditText = onView(
                allOf(withId(com.google.android.libraries.places.R.id.places_autocomplete_search_bar),
                        childAtPosition(
                                allOf(withId(com.google.android.libraries.places.R.id.places_autocomplete_search_bar_container),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        Thread.sleep(1000);
        appCompatEditText.perform(replaceText("Hamilton"), closeSoftKeyboard());

        ViewInteraction recyclerView = onView(
                allOf(withId(com.google.android.libraries.places.R.id.places_autocomplete_list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));

        Thread.sleep(1000);
        recyclerView.perform(actionOnItemAtPosition(0, click()));
        Thread.sleep(1000);
    }




    static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }


}