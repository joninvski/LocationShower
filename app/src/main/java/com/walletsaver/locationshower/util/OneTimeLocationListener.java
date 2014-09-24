package com.walletsaver.locationshower.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import timber.log.Timber;
import java.util.List;
import com.walletsaver.locationshower.exception.NoProviderException;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

public final class OneTimeLocationListener implements  LocationListener {

    private Location mLastLocationReading;
    private final LocationManager mLocationManager;
    private final Bus mBus;
    private final String mLocationSource;
    private boolean mIsRegistred;

    private OneTimeLocationListener(String locationSource, Context context, Bus bus) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationSource = locationSource;
        mBus = bus;
        mIsRegistred = false;
    }

    public static OneTimeLocationListener createLocationListener(Context context, Bus bus, boolean isProviderGps) {
        if (isProviderGps) {
            return createLocationListenerGps(context, bus);
        } else {
            return createLocationListenerNetwork(context, bus);
        }
    }

    public static OneTimeLocationListener createLocationListenerGps(Context context, Bus bus) {
        return new OneTimeLocationListener(LocationManager.GPS_PROVIDER, context, bus);
    }

    public static OneTimeLocationListener createLocationListenerNetwork(Context context, Bus bus) {
        return new OneTimeLocationListener(LocationManager.NETWORK_PROVIDER, context, bus);
    }

    public void register() {
        final long mMinTime = 0;                // default minimum time between new readings
        final float mMinDistance = 0.0f;        // default minimum distance between old and new readings.

        if (!mIsRegistred) {
            mBus.register(this);
            mLocationManager.requestLocationUpdates(mLocationSource, mMinTime, mMinDistance, this);
            mIsRegistred = true;
        }
    }

    public void unregister() {
        if (mIsRegistred) {
            mBus.unregister(this);
            mLocationManager.removeUpdates(this);
            mIsRegistred = false;
        }
    }

    @Override
    public void onLocationChanged(Location currentLocation) {
        Timber.d("Location changed to %s", currentLocation.toString());

        // there is no last location, keep the current location.
        if (mLastLocationReading == null) { mLastLocationReading = currentLocation; }

        // the current location is older than the last location, ignore the current location
        else if (currentLocation.getTime() < mLastLocationReading.getTime()) { return; }

        // the current location is newer than the last locations, keep the current location.
        else { mLastLocationReading = currentLocation; }

        mBus.post(mLastLocationReading);

        mLocationManager.removeUpdates(this);
    }

    @Produce
    public Location publishNewLocationAvailable() {
        Timber.d("Publishing new location to bus %s", mLastLocationReading);
        if (mLastLocationReading != null) {
            return new Location(mLastLocationReading);
        }
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

    public Location getLastKnownLocation() throws NoProviderException {
        List<String> matchingProviders = mLocationManager.getAllProviders();

        Location mostAccurate = null;

        for (String provider : matchingProviders) {
            final Location newLocation = mLocationManager.getLastKnownLocation(provider);

            if (newLocation != null) {
                if (mostAccurate == null) {
                    mostAccurate = newLocation;
                } else {
                    mostAccurate = mostAccurate.getAccuracy() > newLocation.getAccuracy()
                        ?  mostAccurate : newLocation;
                }
            }
        }
        if (mostAccurate == null) {
            throw new NoProviderException("No provider was available");
        }

        mLastLocationReading = mostAccurate;
        return mLastLocationReading;
    }

    public static long age(Location location) {
        return System.currentTimeMillis() - location.getTime();
    }
}
