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

import com.squareup.otto.Bus;
import com.walletsaver.locationshower.exception.NoProviderException;
import com.walletsaver.locationshower.R;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.List;

import timber.log.Timber;
import com.walletsaver.locationshower.LocationShowerApp;
import com.squareup.otto.Subscribe;
import com.squareup.otto.Produce;
import android.widget.Toast;

public class LocationShowerActivity extends FullscreenActivity implements LocationListener {

    private static final long ONE_MIN = 60 * 1000;            // One minutes in milliseconds
    private static final long FIVE_MINS = 5 * ONE_MIN;        // Five minutes in milliseconds

    private long mMinTime = 5000;                             // default minimum time between new readings
    private float mMinDistance = 1000.0f;                     // default minimum distance between old and new readings.

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

    private Bus getBus() {
        return ((LocationShowerApp) getApplication()).getBus();
    }


    @Override
    public void onLocationChanged(Location currentLocation) {
        Timber.d("Location is now %s", currentLocation.toString());

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

        getBus().post(mLastLocationReading);

        mLocationManager.removeUpdates(this);
    }

    @Produce
    public Location publishNewLocationAvailable() {
        Timber.d("Publishing new location to bus %s", mLastLocationReading);
        if(mLastLocationReading != null)
            return new Location(mLastLocationReading);
        return null;
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
        getBus().register(this);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mMinTime, mMinDistance, this);
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
        getBus().unregister(this);
        super.onPause();
    }

    @OnClick(R.id.dummy_button)
    protected void refreshLocation(Button button) {
        Timber.d("Clicked refresh location button");
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mMinTime, mMinDistance, this);
        Toast.makeText(this, "Searching for location", Toast.LENGTH_SHORT).show();
        positionTextView.setText("Updating...");
    }

    private void alertNoProvider() {
        Toast.makeText(this, "No location provider is enabled", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void newLocationAvailable(Location location) {
        Timber.i("New location available %f/%f", location.getLatitude(), location.getLongitude());
        // TODO: React to the event somehow!
        showPosition(location);
    }

    private void showPosition(Location position) {
        double latitude = mLastLocationReading.getLatitude();
        double longitude = mLastLocationReading.getLongitude();
        Timber.d("Location: %f/%f", latitude, longitude);
        String howOld;
        if(age(position) > FIVE_MINS)
            howOld = "Very old";
        else if(age(position) > ONE_MIN)
            howOld = "Old";
        else
            howOld = "Recent";

        positionTextView.setText(String.format("%f\n%f\n\n%s\n\n%s", latitude, longitude, howOld, position.getProvider()));

        final String alertText = String.format(getString(R.string.position_fetched),
                                               mLastLocationReading.getProvider());
        Toast.makeText(this, alertText, Toast.LENGTH_SHORT).show();
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
