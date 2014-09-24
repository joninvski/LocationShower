package com.pifactorial.test;

import android.test.ActivityInstrumentationTestCase2;

import android.widget.EditText;
import android.widget.TextView;

import com.robotium.solo.Solo;

import com.squareup.spoon.Spoon;

import com.walletsaver.locationshower.activity.LocationShowerActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<LocationShowerActivity> {
    private Solo solo;

    public MainActivityTest() {
        super(LocationShowerActivity.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation());
        getActivity();
    }

    public void testMainActivity() {
        int timeout = 8000;

        assertTrue("LocationShowerActivity not found", solo.waitForActivity(LocationShowerActivity.class, timeout));

        LocationShowerActivity activity = getActivity();
        Spoon.screenshot(activity, "initial_state");
    }
}
