package com.trio.sos;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.trio.sos.helper.Constants;

public class Splash extends Activity {

    public static final String TAG = Splash.class.getName();

    //The delay time for Splash Activity to be in foreground
    private final int SPLASH_TIME_OUT = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i;
                SharedPreferences route = getSharedPreferences(
                        Constants.SHARED_PREFERENCE_ROUTE
                        , MODE_PRIVATE);
                String str = route.getString(Constants.SHARED_PREFERENCE_KEY_SPLASH_ROUTE
                        , Constants.SHARED_PREFERENCE_VALUE_SPLASH_ROUTE_LOGIN);
                switch (str) {
                    case Constants.SHARED_PREFERENCE_VALUE_SPLASH_ROUTE_MAIN:
                        i = new Intent(Splash.this, MainActivity.class);
                        break;
                    case Constants.SHARED_PREFERENCE_VALUE_SPLASH_ROUTE_LOGIN:
                        i = new Intent(Splash.this, LoginActivity.class);
                        break;
                    default:
                        i = new Intent(Splash.this, ContactsActivity.class);
                        break;
                }
                i.putExtra(Constants.KEY_INTENT_FROM, TAG);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}