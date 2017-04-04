package com.trio.sos.util;

/**
 * Created by Pranav on 29-03-2017.
 */

public final class Constants {
    //Result Codes
    public static final int SUCCESS=1;
    public static final int FAILURE=2;

    //Request Codes
    public static final int REQUEST_SIGN_IN = 999;
    public static final int REQUEST_AUTHORIZATION = 1000;
    public static final int REQUEST_PERMISSION_LOCATION = 1001;
    public static final int REQUEST_PERMISSION_SEND_SMS = 1002;
    public static final int REQUEST_PERMISSION_WRITE_STORAGE = 1004;
    public static final int REQUEST_PERMISSION_READ_CONTACTS = 1005;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1006;
    public static final int REQUEST_RESOLUTION_SIGN_IN = 1007;
    public static final int REQUEST_CONTACT_PICKER = 1008;

    //Intent Keys
    public static final String KEY_NAME = "NAME";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_PHOTO_URL = "PHOTOURL";

    //SharedPreferences Files
    public static final String SHARED_PREFERENCE_ROUTE="ROUTE";

    //SharedPreference Keys
    public static final String SHARED_PREFERENCE_KEY_SPLASH_ROUTE="DIRECTION";

    //SharedPreference Values
    public static final String SHARED_PREFERENCE_VALUE_SPLASH_ROUTE_MAIN="MAIN";
    public static final String SHARED_PREFERENCE_VALUE_SPLASH_ROUTE_LOGIN="LOGIN";
    public static final String SHARED_PREFERENCE_VALUE_SPLASH_ROUTE_CONTACTS="INFO";

    //Misc
    public static final String GENDER_MALE = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";
    public static final String INTENT_KEY_FROM = "FROM";

    public static final String INTENT_KEY_LOCATION_DATA ="LOCATION_DATA_EXTRA";
    public static final String INTENT_KEY_LOCATION_RESULT ="LOCATION_RESULT_EXTRA";
    public static final int LOCATION_SUCCESS_RESULT = 0;
    public static final int LOCATION_FAILURE_RESULT = 1;
    public static final String LOCATION_RECEIVER = "RECEIVER";
}
