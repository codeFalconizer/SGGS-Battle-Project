package com.trio.sos.helper;

/**
 * Created by Pranav on 29-03-2017.
 */

public class Constants {

    //Request Codes
    public static final int REQUEST_SIGN_IN = 999;
    public static final int REQUEST_AUTHORIZATION = 1000;
    public static final int REQUEST_PERMISSION_LOCATION = 1001;
    public static final int REQUEST_PERMISSION_SEND_SMS = 1002;
    public static final int REQUEST_PERMISSION_READ_STORAGE = 1003;
    public static final int REQUEST_PERMISSION_WRITE_STORAGE = 1004;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1005;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1006;
    public static final int REQUEST_RESOLUTION_SIGN_IN = 1007;

    //Intent Keys
    public static final String KEY_NAME = "NAME";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_PHOTO_URL = "PHOTOURL";

    //SharedPreferences Files
    public static final String SHARED_PREFERENCE_ROUTE="ROUTE";

    //SharedPreference Keys
    public static final String SHARED_PREFERENCE_KEY_ROUTE="DIRECTION";

    //Misc
    public static final String GENDER_MALE = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";
    public static final String KEY_INTENT_FROM = "FROM";

}
