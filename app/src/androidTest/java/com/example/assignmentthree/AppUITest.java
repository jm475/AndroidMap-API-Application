package com.example.assignmentthree;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.app.Instrumentation;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AppUITest {


    @Rule
    public ActivityScenarioRule<MapsActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapsActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");


    /**
     * mapsActivityTest to test the entire UI of the app. Searches for a location in the searchbar and selects the 1st element.
     * Then clicks on a marker to open a new Activity.
     * All the Thread.sleep methods give us a chance to see what is happening
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    @Test
    public void mapsActivityTest() throws UiObjectNotFoundException, InterruptedException {
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

        //Click on a marker with the title "Hamilton"
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject marker = device.findObject(new UiSelector().descriptionContains("Hamilton"));
        Thread.sleep(1000);
        marker.click();
        Thread.sleep(1000);

    }

    private static Matcher<View> childAtPosition(
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
