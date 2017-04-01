package com.trio.sos;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.skyfishjy.library.RippleBackground;
import com.trio.sos.helper.Constants;
import com.trio.sos.repo.Settings;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks{
    public static final String TAG = MainActivity.class.getName();

    RelativeLayout mRootLayout;
    FloatingActionButton mSettingButton;
    FloatingActionButton mProfileButton;
    FloatingActionButton mSignOutButton;
    FloatingActionButton mContactsButton;
    RippleBackground rippleBackground;
    FloatingActionsMenu mFabMenu;
    Button mSosButton;
    Settings mSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialising Settings object
        mSettings = new Settings(this);

        //Binding activity to views
        rippleBackground = (RippleBackground) findViewById(R.id.main_ripple_background);
        mSosButton = (Button) findViewById(R.id.main_button_sos);
        mSettingButton = (FloatingActionButton) findViewById(R.id.main_fab_setting);
        mProfileButton = (FloatingActionButton) findViewById(R.id.main_fab_profile);
        mSignOutButton = (FloatingActionButton) findViewById(R.id.main_fab_signout);
        mContactsButton = (FloatingActionButton) findViewById(R.id.main_fab_contacts);
        mRootLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mFabMenu = (FloatingActionsMenu) findViewById(R.id.main_fab_menu);

        //Setting listeners
        mSosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootLayout.getBackground().setAlpha(255);
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootLayout.getBackground().setAlpha(255);
                Intent i = new Intent(MainActivity.this, InfoActivity.class);
                i.putExtra(Constants.KEY_INTENT_FROM, TAG);
                startActivity(i);
            }
        });

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

        mContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootLayout.getBackground().setAlpha(255);
                Intent i = new Intent(MainActivity.this, ContactsActivity.class);
                i.putExtra(Constants.KEY_INTENT_FROM, TAG);
                startActivity(i);
            }
        });

        mFabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mRootLayout.getBackground().setAlpha(100);
                mSosButton.setEnabled(false);
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
                mSosButton.setEnabled(true);
                mRootLayout.getBackground().setAlpha(255);
                mRootLayout.setOnTouchListener(null);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mFabMenu.collapse();
    }

    private void checkPermissions() {
        //Checking for SMS Permission Changes
        if (!EasyPermissions.hasPermissions(this, android.Manifest.permission.SEND_SMS)) {
            mSettings.setSmsAlertEnabled(false);
            if (!mSettings.isEmailAlertEnabled()){
                mSettings.setSmsAlertEnabled(true);
            }
            mSettings.save();
        }
        //Checking for Location and Storage Access
        if (!EasyPermissions.hasPermissions(this
                , android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || !EasyPermissions.hasPermissions(this
                , android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            final AlertDialog.Builder alertDialogueBuilder = new AlertDialog.Builder(this);
            alertDialogueBuilder.setCancelable(false);
            alertDialogueBuilder.setMessage("Both or one of the permissions for Location or " +
                    "Storage access has been denied due to which the app cannot function");
            alertDialogueBuilder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            alertDialogueBuilder.setPositiveButton("Give Permission", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //RequestPermission
                    dialog.dismiss();
                    requestPermissions();
                }
            });
            alertDialogueBuilder.create().show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    private void requestPermissions() {
        if(!EasyPermissions.hasPermissions(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            EasyPermissions.requestPermissions(this,"Application needs Storage permission to read credentials from storage"
                    ,Constants.REQUEST_PERMISSION_WRITE_STORAGE
                    ,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else if (!EasyPermissions.hasPermissions(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            EasyPermissions.requestPermissions(this,"Application needs Location access to report location to Emergency Contacts"
                    ,Constants.REQUEST_PERMISSION_LOCATION
                    ,Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFabMenu.collapse();
        checkPermissions();
        rippleBackground.startRippleAnimation();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG,"I am Here!");
        if (requestCode==Constants.REQUEST_PERMISSION_WRITE_STORAGE){
            Toast.makeText(this, "Storage Access Permission Granted", Toast.LENGTH_SHORT).show();
        }else if (requestCode == Constants.REQUEST_PERMISSION_LOCATION){
            Toast.makeText(this, "Location Access Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "Permission Denied.Try enabling permission from Android Settings app", Toast.LENGTH_SHORT).show();
    }
}