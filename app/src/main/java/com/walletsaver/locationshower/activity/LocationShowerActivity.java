package com.walletsaver.locationshower.activity;

import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.walletsaver.locationshower.exception.NoProviderException;
import com.walletsaver.locationshower.exception.NoProviderException;
import com.walletsaver.locationshower.LocationShowerApp;
import com.walletsaver.locationshower.R;
import com.walletsaver.locationshower.task.GetAddressTask;
import com.walletsaver.locationshower.util.OneTimeLocationListener;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.Formatter;
import java.util.Locale;

import timber.log.Timber;

public class LocationShowerActivity extends FullscreenActivity {

    // Style for a crouton popup
    private static final Style INFINITE = new Style.Builder().setBackgroundColorValue(R.color.yellow_crouton).build();
    private static final Configuration CONFIGURATION_INFINITE =
        new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE) .build();

    // Constants to check time
    private static final long ONE_MIN = 60 * 1000;          // One minutes in milliseconds
    private static final long FIVE_MINS = 5 * ONE_MIN;      // Five minutes in milliseconds

    private OneTimeLocationListener mLocationListener;      // Listener that discovers the location
    private Crouton mTemporaryLocationCrouton;              // Reference for the crouton popup

    private boolean mIsProviderGps;                         // Indicates if using GPS (true) or network (false)

    // Views injected by butterknife
    @InjectView(R.id.fullscreen_content) TextView positionTextView;

    // -----------------   Activity lifecycle methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Injects required views
        ButterKnife.inject(this);

        mIsProviderGps = false;                 //Starts on network provider
        mTemporaryLocationCrouton = null;
        mLocationListener = OneTimeLocationListener.createLocationListener(this, getBus(), mIsProviderGps);
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

    // -----------------   Buttons onClick callbacks
    @OnClick(R.id.refresh_button)
    protected void refreshLocation(ImageButton button) {
        Timber.d("Clicked refresh location button");
        positionTextView.setText(getString(R.string.update));
        try {
            Location lastLocationReading = mLocationListener.getLastKnownLocation();
            boolean temporaryLocation = true;
            showLocation(lastLocationReading, temporaryLocation);
            mLocationListener.register();
        } catch (NoProviderException e) {
            Timber.e("%s", e);
            alertNoProvider();
        }
    }

    @OnClick(R.id.about_address_button)
    protected void aboutAddress(ImageButton button) {
        Timber.e("Clicked About Address button");

        // Ensure that a Geocoder services is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
                && Geocoder.isPresent()) {

            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
            try {
                final Location currentLocation =  mLocationListener.getLastKnownLocation();
                GetAddressTask task = new GetAddressTask(this, getBus());
                task.execute(currentLocation);
            } catch (NoProviderException e) {
                alertNoProvider();
            }
        }
    }

    @OnClick(R.id.location_provider_button)
    protected void changeLocationProvider(ImageButton button) {
        Timber.d("Clicked change location provider");

        if (mIsProviderGps) {
            button.setImageResource(R.drawable.ic_action_network_wifi);
            Toast.makeText(this, getString(R.string.using_network), Toast.LENGTH_SHORT).show();
        } else {
            button.setImageResource(R.drawable.ic_action_location_searching);
            Toast.makeText(this, getString(R.string.using_gps), Toast.LENGTH_SHORT).show();
        }

        mIsProviderGps = !mIsProviderGps;
        mLocationListener.unregister();
        mLocationListener = OneTimeLocationListener.createLocationListener(this, getBus(), mIsProviderGps);
    }


    // ----------------- Otto bus consumers
    @Subscribe
    public void newAddressAvailable(String address) {
        Timber.i("New address available %s", address);
        Crouton.makeText(this, address, Style.INFO).show();
    }


    @Subscribe
    public void newLocationAvailable(Location location) {
        Timber.i("New location available %f/%f", location.getLatitude(), location.getLongitude());
        showLocation(location);
        mTemporaryLocationCrouton = null;
    }

    // ----------------- Ui changers
    private void showLocation(Location location) {
        boolean temporaryLocation = false;
        showLocation(location, temporaryLocation);
    }

    private void showLocation(Location location, boolean temporaryLocation) {
        Timber.d("Updating the location in the ui %s", temporaryLocation);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String howOld;
        if (OneTimeLocationListener.age(location) > FIVE_MINS) {
            howOld = getString(R.string.very_old);
        } else if (OneTimeLocationListener.age(location) > ONE_MIN) {
            howOld = getString(R.string.old);
        } else {
            howOld = getString(R.string.recent);
        }

        Formatter formatter = new Formatter(Locale.US);
        String text = formatter.format("%f\n%f\n\n%s\n\n%s",
                                       latitude, longitude, howOld, location.getProvider()).toString();
        positionTextView.setText(text);

        if (mTemporaryLocationCrouton != null) {
            Timber.d("Hidding old croutons %s", mTemporaryLocationCrouton);
            Crouton.hide(mTemporaryLocationCrouton);
            Crouton.clearCroutonsForActivity(this);
        }

        if (temporaryLocation) {
            Timber.d("Temporary location setting");
            mTemporaryLocationCrouton = Crouton.makeText(this, getString(R.string.update), INFINITE);
            mTemporaryLocationCrouton.setConfiguration(CONFIGURATION_INFINITE);
            mTemporaryLocationCrouton.show();
        }
    }

    private void alertNoProvider() {
        Toast.makeText(this, getString(R.string.no_provider), Toast.LENGTH_SHORT).show();
        positionTextView.setText(getString(R.string.no_position));
    }


    // ----------------- Util methods
    private Bus getBus() {
        return ((LocationShowerApp) getApplication()).getBus();
    }


}
