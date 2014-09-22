package com.walletsaver.locationshower.activity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.walletsaver.locationshower.R;

import java.util.List;
import timber.log.Timber;
import android.widget.TextView;

public class LocationShowerActivity extends FullscreenActivity {

    private static final long FIVE_MINS = 5 * 60 * 1000; // Five minutes in milliseconds
    private static final String TAG = "LocationShowerActivity";

    private LocationManager mLocationManager;
    private Location mLastLocationReading;

    @InjectView(R.id.dummy_button) Button refreshButton;
    @InjectView(R.id.fullscreen_content) TextView positionTextView;

    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Injects required views
        ButterKnife.inject(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastLocationReading = getLastKnownLocation();
        showPosition(mLastLocationReading);
    }


    @OnClick(R.id.dummy_button)
    protected void refreshPosition(Button button) {
        mLastLocationReading = getLastKnownLocation();
        showPosition(mLastLocationReading);
    }

    private void showPosition(Location position)
    { 
        double latitude = mLastLocationReading.getLatitude();
        double longitude = mLastLocationReading.getLongitude();
        Timber.d("Location: %f/%f", latitude, longitude);
        positionTextView.setText(String.format("%f\n%f", latitude, longitude));
    }

    private Location getLastKnownLocation() {
        List<String> matchingProviders = mLocationManager.getAllProviders();

        for (String provider : matchingProviders) {
            Location location = mLocationManager.getLastKnownLocation(provider);

            if (location != null) {
                Timber.d(TAG, "The choosen provider was: " + provider);
                if (age(location) < FIVE_MINS) {
                    Timber.d("Location is fresh.");
                    return location;
                } else { // Location is old, warn user TODO
                    Timber.d("Location is old.");
                    return location;
                }
            }
        }
        return null;
    }

    private long age(Location location) {
        return System.currentTimeMillis() - location.getTime();
    }
}
