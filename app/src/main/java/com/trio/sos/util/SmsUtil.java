package com.trio.sos.util;

import android.app.Application;
import android.location.Location;

public class SmsUtil {

    private final String mMapsUrl = "https://www.google.com/maps/place/";
    private Location location;
    private String link;

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getLocationMessage() {
        String message = null;
        if (location == null){
            return null;
        }else{
            message = "Save me"
                    + "\nLocation on Map: " + mMapsUrl + location.getLatitude() + "," + location.getLongitude();
        }
        return message;
    }

    public String getLinkMessage() {
        String message = null;
        if (location == null){
            return null;
        }else{
            message = "Save me"
                    + "\nLink to Video: " + link;
        }
        return message;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
