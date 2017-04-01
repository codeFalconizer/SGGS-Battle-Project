package com.trio.sos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ContactsActivity extends AppCompatActivity {

    public static final String TAG = ContactsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Emergency Contact Info");
        setContentView(R.layout.activity_contacts);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
