package com.trio.sos;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.skyfishjy.library.RippleBackground;
import com.trio.sos.repo.EmergencyContacts;
import com.trio.sos.repo.Settings;
import com.trio.sos.services.FetchAddressIntentService;
import com.trio.sos.util.Constants;
import com.trio.sos.util.SmsUtil;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks, LocationListener {
    public static final String TAG = MainActivity.class.getName();

    private static final int MINUTES = 1000 * 60;
    RelativeLayout mRootLayout;
    FloatingActionButton mSettingButton;
    FloatingActionButton mProfileButton;
    FloatingActionButton mSignOutButton;
    FloatingActionButton mContactsButton;
    RippleBackground rippleBackground;
    FloatingActionsMenu mFabMenu;
    TextView mTextAddress;
    TextView mTextLatitude;
    TextView mTextLongitude;
    Button mSosButton;
    Settings mSettings;
    LocationManager mLocationManager;
    Location currentBestLocation;
    private AddressResultReceiver mResultReceiver;
    EmergencyContacts contact;

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String mAddressOutput = resultData.getString(Constants.INTENT_KEY_LOCATION_RESULT);
            if (resultCode == Constants.LOCATION_SUCCESS_RESULT) {
                mTextAddress.setText(mAddressOutput);
            }
        }
    }

    private class SendSMS extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            if (params.length != 1) {
                Log.e(TAG, "Invalid no of parameters to SendSms.exceute(), expected 1, found " + params.length);
                return Constants.FAILURE;
            }
            String message = params[0];
            Log.i(TAG, message);
            SmsManager smsManager = SmsManager.getDefault();
            List<EmergencyContacts.Person> people = contact.getAllContacts();
            smsManager.sendTextMessage(people.get(0).getNumber(), null, message, null, null);
            smsManager.sendTextMessage(people.get(1).getNumber(), null, message, null, null);
            return Constants.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);
            if (resultCode == Constants.SUCCESS) {
                Toast.makeText(MainActivity.this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "SMS sending failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class BetterLocation extends AsyncTask<Location, Void, Location> {

        @Override
        protected Location doInBackground(Location... params) {
            if (params.length != 2) {
                Log.e(TAG, "Invalid number of parameters to BetterLocation.execute()");
                return null;
            } else if (params[0] == null) {
                Log.e(TAG, "Newer location cannot be null");
            }
            Location location1 = params[0];
            Location location2 = params[1];
            if (isBetterLocation(location1, location2)) {
                return location1;
            } else {
                return location2;
            }
        }

        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);
            if (location == null) {
                return;
            }
            currentBestLocation = location;
            String latitude = "Latitude : " + String.valueOf(currentBestLocation.getLatitude());
            String longitude = "Longitude : " + String.valueOf(currentBestLocation.getLongitude());
            mTextLatitude.setText(latitude);
            mTextLongitude.setText(longitude);
            Intent intent = new Intent(MainActivity.this, FetchAddressIntentService.class);
            intent.putExtra(Constants.INTENT_KEY_LOCATION_DATA, currentBestLocation);
            intent.putExtra(Constants.LOCATION_RECEIVER, mResultReceiver);
            startService(intent);
        }

        private boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > MINUTES;
            boolean isSignificantlyOlder = timeDelta < -MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
            return false;
        }

        /**
         * Checks whether two providers are the same
         */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //Initialising Settings object
        mSettings = new Settings(this);
        contact = new EmergencyContacts(this);
        mResultReceiver = new AddressResultReceiver(new Handler());
        Log.d(TAG, mResultReceiver.toString());
        //Binding activity to views
        rippleBackground = (RippleBackground) findViewById(R.id.main_ripple_background);
        mSosButton = (Button) findViewById(R.id.main_button_sos);
        mSettingButton = (FloatingActionButton) findViewById(R.id.main_fab_setting);
        mProfileButton = (FloatingActionButton) findViewById(R.id.main_fab_profile);
        mSignOutButton = (FloatingActionButton) findViewById(R.id.main_fab_signout);
        mContactsButton = (FloatingActionButton) findViewById(R.id.main_fab_contacts);
        mRootLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mFabMenu = (FloatingActionsMenu) findViewById(R.id.main_fab_menu);
        mTextAddress = (TextView) findViewById(R.id.main_text_address);
        mTextLatitude = (TextView) findViewById(R.id.main_text_latitude);
        mTextLongitude = (TextView) findViewById(R.id.main_text_longitude);
        //Setting listeners
        mSosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsUtil smsutil = new SmsUtil();
                if (currentBestLocation != null) {
                    if (mSettings.isSmsAlertEnabled()) {
                        smsutil.setLocation(currentBestLocation);
                        new SendSMS().execute(smsutil.getMessage());
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Still Fetching Location", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                mRootLayout.getBackground().setAlpha(255);
                mSignOutButton.getBackground().setAlpha(255);
                startActivity(i);
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, InfoActivity.class);
                i.putExtra(Constants.INTENT_KEY_FROM, TAG);
                mRootLayout.getBackground().setAlpha(255);
                mSignOutButton.getBackground().setAlpha(255);
                startActivity(i);
            }
        });

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.putExtra(Constants.INTENT_KEY_FROM, TAG);
                mRootLayout.getBackground().setAlpha(255);
                mSignOutButton.getBackground().setAlpha(255);
                startActivity(i);
                finish();
            }
        });

        mContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ContactsActivity.class);
                i.putExtra(Constants.INTENT_KEY_FROM, TAG);
                mRootLayout.getBackground().setAlpha(255);
                mSignOutButton.getBackground().setAlpha(255);
                startActivity(i);
            }
        });

        mFabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mRootLayout.getBackground().setAlpha(100);
                mSosButton.setEnabled(false);
                mSosButton.getBackground().setAlpha(100);
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
                mSosButton.getBackground().setAlpha(255);
                mRootLayout.setOnTouchListener(null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTextAddress.setText(getResources().getString(R.string.main_address_initial_text));
        mTextLatitude.setText(getResources().getString(R.string.main_coordinates_initial_text));
        mTextLongitude.setText("");
        try {
            currentBestLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (currentBestLocation != null) {
                String latitude = "Latitude : " + currentBestLocation.getLatitude();
                String longitude = "Longitude : " + currentBestLocation.getLongitude();
                mTextLatitude.setText(latitude);
                mTextLongitude.setText(longitude);
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
            if (currentBestLocation != null) {
                Intent intent = new Intent(this, FetchAddressIntentService.class);
                intent.putExtra(Constants.INTENT_KEY_LOCATION_DATA, currentBestLocation);
                intent.putExtra(Constants.LOCATION_RECEIVER, mResultReceiver);
                startService(intent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            checkPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFabMenu.collapse();
        mSettings.update();
        rippleBackground.startRippleAnimation();
        checkPermissions();
        checkLocationService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(this);
        Log.i(TAG, "Stop getting location updates");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mFabMenu.collapse();
        finish();
    }

    private void checkPermissions() {
        //Checking for SMS Permission Changes
        if (!EasyPermissions.hasPermissions(this, android.Manifest.permission.SEND_SMS)) {
            mSettings.setSmsAlertEnabled(false);
            if (!mSettings.isEmailAlertEnabled()) {
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
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this, "Application needs Storage permission to read credentials from storage"
                    , Constants.REQUEST_PERMISSION_WRITE_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(this, "Application needs Location access to report location to Emergency Contacts"
                    , Constants.REQUEST_PERMISSION_LOCATION
                    , Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "I am Here!");
        if (requestCode == Constants.REQUEST_PERMISSION_WRITE_STORAGE) {
            Toast.makeText(this, "Storage Access Permission Granted", Toast.LENGTH_SHORT).show();
        } else if (requestCode == Constants.REQUEST_PERMISSION_LOCATION) {
            Toast.makeText(this, "Location Access Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "Permission Denied.Try enabling permission from Android Settings app", Toast.LENGTH_SHORT).show();
    }

    private void checkLocationService() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(false);
            dialog.setMessage("Location Service Disabled.Please turn on Location.");

            dialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });

            dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        new BetterLocation().execute(location, currentBestLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}