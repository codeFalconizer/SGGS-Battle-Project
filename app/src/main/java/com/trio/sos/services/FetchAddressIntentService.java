package com.trio.sos.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.trio.sos.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Pranav on 03-04-2017.
 */

public class FetchAddressIntentService extends IntentService {

    public static final String TAG = FetchAddressIntentService.class.getName();
    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
     /*
     * @param name Used to name the worker thread, important only for debugging.
     */
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mReceiver = intent.getParcelableExtra(Constants.LOCATION_RECEIVER);
        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.INTENT_KEY_LOCATION_DATA);

        List<Address> addresses = null;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid Latitude and Longitude used";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }
        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No Address found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.LOCATION_FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address Found");
            deliverResultToReceiver(Constants.LOCATION_SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.INTENT_KEY_LOCATION_RESULT, message);
        mReceiver.send(resultCode, bundle);
    }
}
