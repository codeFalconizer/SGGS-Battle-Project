package com.trio.sos;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.trio.sos.util.Constants;
import com.trio.sos.repo.EmergencyContacts;
import com.trio.sos.repo.LoggedUser;
import com.trio.sos.repo.Settings;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, EasyPermissions.PermissionCallbacks {

    public static final String TAG = LoginActivity.class.getName();

    GoogleApiClient mGoogleApiClient;

    //Permission Deny Flags
    boolean mSmsDenyFlag = false;
    boolean mLocationDenyFlag = false;
    boolean mStorageDenyFlag = false;
    boolean mContactsDenyFlag = false;
    Settings mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSettings = new Settings(this);

        //Creating objects for Google Sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Settings.init(this);
        SignInButton signInButton = (SignInButton) findViewById(R.id.login_activity_button_google_signin);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
                signOut();
                signIn();
            }
        });

        checkForSignOutRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Checking for Permissions and requesting if not present
        requestPermissions();
        //Connecting API Client
        mGoogleApiClient.connect();
    }

    private void requestPermissions() {
        if (!mSmsDenyFlag && !EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)) {
            EasyPermissions.requestPermissions(this
                    , "Application needs SMS permission to send SMS to emergency contacts"
                    , Constants.REQUEST_PERMISSION_SEND_SMS
                    , Manifest.permission.SEND_SMS);
        } else if (!mStorageDenyFlag && !EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this
                    , "Application needs Storage permission to read credentials from storage"
                    , Constants.REQUEST_PERMISSION_WRITE_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else if (!mLocationDenyFlag && !EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(this
                    , "Application needs Location access to report location to Emergency Contacts"
                    , Constants.REQUEST_PERMISSION_LOCATION
                    , Manifest.permission.ACCESS_FINE_LOCATION);
        } else if (!mContactsDenyFlag && !EasyPermissions.hasPermissions(this,Manifest.permission.READ_CONTACTS)){
            EasyPermissions.requestPermissions(this
                    , "Contacts permission is required to enable picking of Emergency Contacts"
                    , Constants.REQUEST_PERMISSION_READ_CONTACTS
                    , Manifest.permission.READ_CONTACTS);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        requestPermissions();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == Constants.REQUEST_PERMISSION_SEND_SMS) {
            mSmsDenyFlag = true;
            mSettings.setSmsAlertEnabled(false);
            mSettings.save();
        }else if (requestCode == Constants.REQUEST_PERMISSION_WRITE_STORAGE) {
            mStorageDenyFlag = true;
        }else if (requestCode == Constants.REQUEST_PERMISSION_LOCATION) {
            mLocationDenyFlag = true;
        }else if (requestCode == Constants.REQUEST_PERMISSION_READ_CONTACTS) {
            mContactsDenyFlag = true;
        }
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void checkForSignOutRequest() {
        Intent intent = getIntent();
        if (intent.getStringExtra(Constants.INTENT_KEY_FROM).equals(MainActivity.TAG)) {
            SharedPreferences route = getSharedPreferences(
                    Constants.SHARED_PREFERENCE_ROUTE
                    , MODE_PRIVATE);
            route.edit().clear().apply();
            Settings settings = new Settings(this);
            settings.clear();
            LoggedUser user = new LoggedUser(this);
            user.clear();
            EmergencyContacts emergencyContacts = new EmergencyContacts(this);
            emergencyContacts.clear();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Constants.REQUEST_SIGN_IN && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "Login Successfull", Toast.LENGTH_SHORT).show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();

        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Intent data = new Intent(this, InfoActivity.class);
            try {
                data.putExtra(Constants.INTENT_KEY_FROM, "" + TAG);
                data.putExtra(Constants.KEY_NAME, "" + acct.getDisplayName());
                data.putExtra(Constants.KEY_EMAIL, "" + acct.getEmail());
                data.putExtra(Constants.KEY_PHOTO_URL, ""+acct.getPhotoUrl());

            } catch (Exception e) {
                e.printStackTrace();
            }
            updateUI(data);
        } else {

            // Login Not successfull
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            if (result.getStatus().getStatusMessage() != null) {
                Toast.makeText(this, "" + result.getStatus().getStatusMessage()
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI(Intent data) {
        if (data != null) {
            startActivity(data);
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }


    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, Constants.REQUEST_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}