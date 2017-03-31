package com.trio.sos;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.trio.sos.helper.Constants;

/**
 * Created by Pranav on 20-03-2017.
 */

public class Splash extends Activity {

    public static final String TAG = Splash.class.getName();
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i;
                SharedPreferences route = getSharedPreferences(
                        Constants.SHARED_PREFERENCE_ROUTE
                            ,MODE_PRIVATE);
                if (route.getBoolean(Constants.SHARED_PREFERENCE_KEY_ROUTE,false)){
                    i = new Intent(Splash.this, MainActivity.class);
                }else{
                    i = new Intent(Splash.this, LoginActivity.class);
                }
                i.putExtra(Constants.KEY_INTENT_FROM,TAG);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}