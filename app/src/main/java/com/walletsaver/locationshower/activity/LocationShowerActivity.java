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
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.walletsaver.locationshower.R;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.List;

import timber.log.Timber;
import com.walletsaver.locationshower.exception.NoProviderException;

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
        try {
            mLastLocationReading = getLastKnownLocation();
            showPosition(mLastLocationReading);
        } catch (NoProviderException e) {
            alertNoProvider();
        }
    }


    @OnClick(R.id.dummy_button)
    protected void refreshPosition(Button button) {
        try {
            mLastLocationReading = getLastKnownLocation();
            showPosition(mLastLocationReading);
            final String alertText = String.format(getString(R.string.position_fetched),
                                                   mLastLocationReading.getProvider());
            Crouton.makeText(this, alertText, Style.CONFIRM).show();
        } catch (NoProviderException e) {
            alertNoProvider();
        }
    }

    private void alertNoProvider() {
        Crouton.makeText(this, "No location provider is enabled", Style.ALERT).show();
    }

    private void showPosition(Location position) {
        double latitude = mLastLocationReading.getLatitude();
        double longitude = mLastLocationReading.getLongitude();
        Timber.d("Location: %f/%f", latitude, longitude);
        positionTextView.setText(String.format("%f\n%f", latitude, longitude));
    }

    private Location getLastKnownLocation() throws NoProviderException {
        List<String> matchingProviders = mLocationManager.getAllProviders();

        Location mostAccurate = null;

        for (String provider : matchingProviders) {
            final Location newLocation = mLocationManager.getLastKnownLocation(provider);
            Timber.d("Provider: %s", provider);

            if (newLocation != null) {
                if(mostAccurate == null)
                    mostAccurate = newLocation;
                else {
                    mostAccurate = mostAccurate.getAccuracy() > newLocation.getAccuracy() ?
                    mostAccurate : newLocation;
                }
            }
        }
        if (mostAccurate == null) {
            throw new NoProviderException("No provider was available");
        }

        return mostAccurate;
    }

    private long age(Location location) {
        return System.currentTimeMillis() - location.getTime();
    }
}
