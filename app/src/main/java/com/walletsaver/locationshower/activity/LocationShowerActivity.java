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
import android.widget.Button;
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

import java.util.List;

import timber.log.Timber;

public class LocationShowerActivity extends FullscreenActivity {

    private static final long ONE_MIN = 60 * 1000;            // One minutes in milliseconds
    private static final long FIVE_MINS = 5 * ONE_MIN;        // Five minutes in milliseconds

    private long mMinTime = 5000;                             // default minimum time between new readings
    private float mMinDistance = 1000.0f;                     // default minimum distance between old and new readings.

    private OneTimeLocationListener mLocationListener;

    @InjectView(R.id.dummy_button) Button refreshButton;
    @InjectView(R.id.fullscreen_content) TextView positionTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Injects required views
        ButterKnife.inject(this);

        mLocationListener = OneTimeLocationListener.createLocationListenerNetwork(this, getBus());
    }

    private Bus getBus() {
        return ((LocationShowerApp) getApplication()).getBus();
    }


    @Override
    protected void onResume() {
        super.onResume();

        getBus().register(this);
        try{
            Location lastLocationReading = mLocationListener.getLastKnownLocation();
            showLocation(lastLocationReading);
        }
        catch (NoProviderException e){
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
    protected void refreshLocation(Button button) {
        Timber.d("Clicked refresh location button");
        mLocationListener.register(mMinTime, mMinDistance);
        Toast.makeText(this, "Searching for location", Toast.LENGTH_SHORT).show();
        positionTextView.setText("Updating...");
    }

    private void alertNoProvider() {
        Toast.makeText(this, "No location provider is enabled", Toast.LENGTH_SHORT).show();
        positionTextView.setText("...");
    }

    @Subscribe
    public void newLocationAvailable(Location location) {
        Timber.i("New location available %f/%f", location.getLatitude(), location.getLongitude());
        // TODO: React to the event somehow!
        showLocation(location);
    }

    private void showLocation(Location location) {
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

        positionTextView.setText(
                String.format("%f\n%f\n\n%s\n\n%s", latitude, longitude, howOld, location.getProvider()));
    }
}
