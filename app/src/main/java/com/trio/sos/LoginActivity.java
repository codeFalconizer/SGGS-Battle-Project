package com.trio.sos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import com.trio.sos.helper.Constants;
import com.trio.sos.repo.EmergencyContacts;
import com.trio.sos.repo.LoggedUser;
import com.trio.sos.repo.Settings;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    GoogleApiClient mGoogleApiClient;
    public static final String TAG = LoginActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
                signOut();
                signIn();
            }
        });
        checkForSignOutRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private void checkForSignOutRequest() {
        Intent intent = getIntent();
        if (intent.getStringExtra(Constants.KEY_INTENT_FROM).equals(MainActivity.TAG)){
            SharedPreferences route = getSharedPreferences(
                    Constants.SHARED_PREFERENCE_ROUTE
                    ,MODE_PRIVATE);
            route.edit().clear().commit();
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
                data.putExtra(Constants.KEY_INTENT_FROM,""+TAG);
                data.putExtra(Constants.KEY_NAME, "" + acct.getDisplayName());
                data.putExtra(Constants.KEY_EMAIL, "" + acct.getEmail());
                data.putExtra(Constants.KEY_PHOTO_URL,acct.getPhotoUrl());

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
                    public void onResult(Status status) {
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, Constants.REQUEST_SIGN_IN);
    }
}
