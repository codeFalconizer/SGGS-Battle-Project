package com.trio.sos.util;

import android.app.Application;
import android.location.Location;

public class SmsUtil {

    private final String mMapsUrl = "https://www.google.com/maps/place/";
    private Location location;

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getMessage() {
        String message = null;
        if (location == null){
            return null;
        }else{
            message = "Sent from Save Me app"
                    + "\nShow location on Map: " + mMapsUrl + location.getLatitude() + "," + location.getLongitude();
        }
        return message;
    }
}
