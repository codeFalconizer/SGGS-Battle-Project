package com.trio.sos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.skyfishjy.library.RippleBackground;
import com.trio.sos.helper.Constants;

/**
 * Created by Pranav on 29-03-2017.
 */

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getName();

    RelativeLayout mRootLayout;
    FloatingActionButton mSettingButton;
    FloatingActionButton mProfileButton;
    FloatingActionButton mSignOutButton;
    RippleBackground rippleBackground;
    FloatingActionsMenu mFabMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rippleBackground = (RippleBackground) findViewById(R.id.main_ripple_background);


        mSettingButton = (FloatingActionButton) findViewById(R.id.main_fab_setting);
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootLayout.getBackground().setAlpha(255);
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        mProfileButton = (FloatingActionButton) findViewById(R.id.main_fab_profile);
        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootLayout.getBackground().setAlpha(255);
                Intent i = new Intent(MainActivity.this, InfoActivity.class);
                i.putExtra(Constants.KEY_INTENT_FROM, TAG);
                startActivity(i);
            }
        });
        mSignOutButton = (FloatingActionButton) findViewById(R.id.main_fab_signout);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootLayout.getBackground().setAlpha(255);
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.putExtra(Constants.KEY_INTENT_FROM, TAG);
                startActivity(i);
                finish();
            }
        });
        mRootLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mFabMenu = (FloatingActionsMenu) findViewById(R.id.main_fab_menu);
        mFabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mRootLayout.getBackground().setAlpha(100);
                mRootLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mFabMenu.collapse();
                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                mRootLayout.getBackground().setAlpha(255);
                mRootLayout.setOnTouchListener(null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFabMenu.collapse();
        rippleBackground.startRippleAnimation();
    }
}
