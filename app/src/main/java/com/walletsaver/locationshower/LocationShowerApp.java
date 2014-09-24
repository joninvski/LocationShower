package com.walletsaver.locationshower;

import android.app.Application;

import com.squareup.otto.Bus;

import static timber.log.Timber.DebugTree;

import timber.log.Timber;

/**
 * Implements the Showcase android application.
 *
 * Responsible to initializing the otto bus and the timber
 * debugtree for all the app
 */
public class LocationShowerApp extends Application {

    private static Bus mBus; // The publish subscribe bus

    /**
     * Build object graph on creation so that objects are available.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Timber is a better lop option.
        Timber.plant(new DebugTree());
        mBus = new Bus();
    }

    public Bus getBus() {
        return mBus;
    }
}
