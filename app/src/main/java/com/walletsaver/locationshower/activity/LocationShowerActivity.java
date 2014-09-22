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

public class LocationShowerActivity extends FullscreenActivity {

    private static final long FIVE_MINS = 5 * 60 * 1000; // Five minutes in milliseconds
    private static final String TAG = "LocationShowerActivity";

    private LocationManager mLocationManager;
    private Location mLastLocationReading;

    @InjectView(R.id.dummy_button) Button refreshButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Injects required views
        ButterKnife.inject(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null)
            finish();
    }

    @OnClick(R.id.dummy_button)
    protected void refreshPosition(Button button) {
        mLastLocationReading = getLastKnownLocation();
        Log.d(TAG, "Location: " + mLastLocationReading.getLatitude() + " / " + mLastLocationReading.getLongitude());

        return ;
    }

    private Location getLastKnownLocation() {
        List<String> matchingProviders = mLocationManager.getAllProviders();

        for (String provider : matchingProviders) {

            // Note: Uncomment these lines for coursera tests
            //if (!provider.equals(LocationManager.NETWORK_PROVIDER))
            //  continue;

            Location location = mLocationManager.getLastKnownLocation(provider);

            if (location != null) {
                Log.d(TAG, "The choosen provider was: " + provider);
                if (age(location) < FIVE_MINS) {
                    Log.d(TAG, "Location is fresh.");
                    return location;
                } else { // Location is old, warn user TODO
                    Log.d(TAG, "Location is old.");
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
