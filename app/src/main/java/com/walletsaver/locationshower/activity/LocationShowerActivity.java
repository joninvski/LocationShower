package com.walletsaver.locationshower.activity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
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

import com.walletsaver.locationshower.exception.NoProviderException;
import com.walletsaver.locationshower.R;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.List;

import timber.log.Timber;

public class LocationShowerActivity extends FullscreenActivity implements LocationListener {

    private static final long FIVE_MINS = 5 * 60 * 1000;        // Five minutes in milliseconds

    private long mMinTime = 5000;                               // default minimum time between new readings
    private float mMinDistance = 1000.0f;                       // default minimum distance between old and new readings.

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
        if (mLocationManager == null) /* TODO Handle this error */
            finish();
    }


    @Override 
    public void onLocationChanged(Location currentLocation) {
 
        Timber.d("MINE", "Location is now %s", currentLocation.toString());

        // 1) If there is no last location, keep the current location. 
        if (mLastLocationReading == null) {
            mLastLocationReading = currentLocation;
        } 
 
 
        // 2) If the current location is older than the last location, ignore the current location 
        else if (currentLocation.getTime() < mLastLocationReading.getTime())
            return; 
 
 
        // 3) If the current location is newer than the last locations, keep the current location. 
        else { 
            mLastLocationReading = currentLocation;
        } 
    } 
 
 
    @Override 
    public void onProviderDisabled(String provider) {
        // not implemented 
    } 
 
 
    @Override 
    public void onProviderEnabled(String provider) {
        // not implemented 
    } 
 
 
    @Override 
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // not implemented 
    } 

    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mMinTime, mMinDistance, this);
        try {
            mLastLocationReading = getLastKnownLocation();

            showPosition(mLastLocationReading);
        } catch (NoProviderException e) {
            alertNoProvider();
        }
    }

    @Override 
    protected void onPause() { 
        mLocationManager.removeUpdates(this);
        super.onPause(); 
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
