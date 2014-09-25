package com.walletsaver.locationshower.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import com.squareup.otto.Bus;
import java.util.List;
import timber.log.Timber;
import java.io.IOException;
import android.location.Address;
import java.util.Locale;
import android.location.Geocoder;

/**
* A subclass of AsyncTask that calls getFromLocation() in the
* background. The class definition has these generic types:
*
* Location - A Location object containing
* the current location.
* Void     - indicates that progress units are not used
* String   - An address passed to onPostExecute()
*/
public class GetAddressTask extends AsyncTask<Location, Void, String> {
    private Context mContext;
    private Bus mBus;

    public GetAddressTask(Context context, Bus bus) {
        super();
        mContext = context;
        mBus = bus;
        bus.register(this);
    }

    /**
     * Get a Geocoder instance, get the latitude and longitude.
     * look up the address, and return it
     *
     * @params params One or more Location objects
     * @return A string containing the address of the current
     * location, or an empty string if no address can be found,
     * or an error message
     */
    @Override
    protected String doInBackground(Location... params) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        // Get the current location from the input parameter list
        Location loc = params[0];
        // Create a list to contain the result address
        List<Address> addresses = null;
        try {
            int numberAddresses = 1;
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), numberAddresses);
        } catch (IOException e1) {
            Timber.e("IO Exception in getFromLocation()");
            e1.printStackTrace();
            return ("IO Exception trying to get address");
        } catch (IllegalArgumentException e2) {
            // Error message to post in the log
            String errorString = "Illegal arguments " + Double.toString(loc.getLatitude())
                + " , " + Double.toString(loc.getLongitude()) + " passed to address service";
            Timber.e(errorString);
            e2.printStackTrace();
            return errorString;
        }
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            // Get the first address
            Address address = addresses.get(0);
            /*
             * Format the first line of address (if available),
             * city, and country name.
             */
            String addressText = String.format(
                                     "%s, %s",
                                     // If there's a street address, add it
                                     address.getMaxAddressLineIndex() > 0
                                     ? address.getAddressLine(0) : "",
                                     // The country of the address
                                     address.getCountryName());
            // Return the text
            return addressText;
        } else {
            return "No address found";
        }
    }

    /**
    * A method that's called once doInBackground() completes. Turn
    * off the indeterminate activity indicator and set
    * the text of the UI element that shows the address. If the
    * lookup failed, display the error message.
    */
    @Override
    protected void onPostExecute(String address) {
        Timber.i("Address is %s", address);

        // Publish the results of the lookup the otto bus
        mBus.post(address);

        // This task has done its job. No longer required to be registered
        mBus.unregister(this);
    }
}
