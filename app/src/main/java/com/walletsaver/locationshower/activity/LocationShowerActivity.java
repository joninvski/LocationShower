package com.walletsaver.locationshower.activity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.walletsaver.locationshower.exception.NoProviderException;
import com.walletsaver.locationshower.LocationShowerApp;
import com.walletsaver.locationshower.R;
import com.walletsaver.locationshower.util.OneTimeLocationListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Configuration;

import java.util.List;

import timber.log.Timber;

public class LocationShowerActivity extends FullscreenActivity {

    private static final Style INFINITE = new Style.Builder().setBackgroundColorValue(R.color.yellow_crouton).build();
    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
    .setDuration(Configuration.DURATION_INFINITE)
    .build();

    private static final long ONE_MIN = 60 * 1000;            // One minutes in milliseconds
    private static final long FIVE_MINS = 5 * ONE_MIN;        // Five minutes in milliseconds

    private OneTimeLocationListener mLocationListener;
    private Crouton mTemporaryLocationCrouton;

    private boolean mIsProviderGps;

    @InjectView(R.id.dummy_button) ImageButton refreshButton;
    @InjectView(R.id.fullscreen_content) TextView positionTextView;
    @InjectView(R.id.location_provider_button) ImageButton locationProviderButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Injects required views
        ButterKnife.inject(this);
        mIsProviderGps = false;
        mTemporaryLocationCrouton = null;
        mLocationListener = OneTimeLocationListener.createLocationListener(this, getBus(), mIsProviderGps);
    }

    private Bus getBus() {
        return ((LocationShowerApp) getApplication()).getBus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getBus().register(this);
        try {
            Location lastLocationReading = mLocationListener.getLastKnownLocation();
            showLocation(lastLocationReading);
        } catch (NoProviderException e) {
            Timber.e("%s", e);
            alertNoProvider();
        }
    }

    @Override
    protected void onPause() {
        mLocationListener.unregister();
        getBus().unregister(this);
        super.onPause();
    }

    @OnClick(R.id.dummy_button)
    protected void refreshLocation(ImageButton button) {
        Timber.d("Clicked refresh location button");
        positionTextView.setText("Updating...");
        try {
            Location lastLocationReading = mLocationListener.getLastKnownLocation();
            boolean temporaryLocation = true;
            showLocation(lastLocationReading, true );
        } catch (NoProviderException e) {
            Timber.e("%s", e);
            alertNoProvider();
        }
        mLocationListener.register();
    }

    @OnClick(R.id.location_provider_button)
    protected void changeLocationProvider(ImageButton button) {
        Timber.d("Clicked change location provider");

        if(mIsProviderGps) {
            button.setImageResource(R.drawable.ic_action_network_wifi);
            Toast.makeText(this, "Now using network", Toast.LENGTH_SHORT).show();
        } else {
            button.setImageResource(R.drawable.ic_action_location_searching);
            Toast.makeText(this, "Now using gps", Toast.LENGTH_SHORT).show();
        }

        mIsProviderGps = !mIsProviderGps;
        mLocationListener.unregister();
        mLocationListener = OneTimeLocationListener.createLocationListener(this, getBus(), mIsProviderGps);
    }

    private void alertNoProvider() {
        Toast.makeText(this, "No location provider is enabled", Toast.LENGTH_SHORT).show();
        positionTextView.setText("...");
    }

    @Subscribe
    public void newLocationAvailable(Location location) {
        Timber.i("New location available %f/%f", location.getLatitude(), location.getLongitude());
        showLocation(location);
        mTemporaryLocationCrouton = null;
    }

    private void showLocation(Location location) {
        boolean temporaryLocation = false;
        showLocation(location, temporaryLocation);
    }

    private void showLocation(Location location, boolean temporaryLocation) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Timber.d("Location: %f/%f", latitude, longitude);
        String howOld;
        if(OneTimeLocationListener.age(location) > FIVE_MINS)
            howOld = "Very old";
        else if(OneTimeLocationListener.age(location) > ONE_MIN)
            howOld = "Old";
        else
            howOld = "Recent";

        String text = String.format("%f\n%f\n\n%s\n\n%s", latitude, longitude, howOld, location.getProvider());
        positionTextView.setText(text);

        if(mTemporaryLocationCrouton != null) {
            Timber.d("Hiding old croutong2");
            Crouton.hide(mTemporaryLocationCrouton);
            Crouton.clearCroutonsForActivity(this);
        }

        if(temporaryLocation) {
            mTemporaryLocationCrouton = Crouton.makeText(this, "Temporary Location", INFINITE);
            mTemporaryLocationCrouton.setConfiguration(CONFIGURATION_INFINITE);
            mTemporaryLocationCrouton.show();
        }
    }
}
