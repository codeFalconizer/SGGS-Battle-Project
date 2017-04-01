package com.trio.sos;

import android.*;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.trio.sos.helper.Constants;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener,EasyPermissions.PermissionCallbacks {

    public static final String TAG = ContactsActivity.class.getName();
    EditText mEditName0, mEditContact0, mEditEmail0;
    EditText mEditName1, mEditContact1, mEditEmail1;
    ImageButton mContactPicker0, mContactPicker1;
    EditText mEditName, mEditContact, mEditEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Emergency Contact Info");
        setContentView(R.layout.activity_contacts);

        //binding Activity to views
        mEditName0 = (EditText) findViewById(R.id.contact_edit_name_0);
        mEditName1 = (EditText) findViewById(R.id.contact_edit_name_1);
        mEditContact0 = (EditText) findViewById(R.id.contact_edit_contact_0);
        mEditContact1 = (EditText) findViewById(R.id.contact_edit_contact_1);
        mEditEmail0 = (EditText) findViewById(R.id.contact_edit_email_0);
        mEditEmail1 = (EditText) findViewById(R.id.contact_edit_email_1);
        mContactPicker0 = (ImageButton) findViewById(R.id.contact_contact_picker_0);
        mContactPicker1 = (ImageButton) findViewById(R.id.contact_contact_picker_1);

        //Setting Listeners
        mContactPicker0.setOnClickListener(this);
        mContactPicker1.setOnClickListener(this);
    }

    private void requestPermission(){
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS)){
            EasyPermissions.requestPermissions(this
                    , "Contacts permission is required to enable picking of Emergency Contacts"
                    , Constants.REQUEST_PERMISSION_READ_CONTACTS
                    , Manifest.permission.READ_CONTACTS);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.contact_contact_picker_0) {
            mEditEmail=mEditEmail0;
            mEditName=mEditName0;
            mEditContact=mEditContact0;
            pickContact();
        } else if (v.getId() == R.id.contact_contact_picker_1) {
            mEditEmail=mEditEmail1;
            mEditName=mEditName1;
            mEditContact=mEditContact1;
            pickContact();
        }
    }

    public void pickContact() {
        if (EasyPermissions.hasPermissions(this,Manifest.permission.READ_CONTACTS)){
            Intent contactPickerIntent =
                    new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(contactPickerIntent, Constants.REQUEST_CONTACT_PICKER);
        }else
            requestPermission();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be using multiple startActivityForReslut
            switch (requestCode) {
                case Constants.REQUEST_CONTACT_PICKER:
                    fillDataFromContact(data);
                    break;
            }
        } else {
            Toast.makeText(this, "Failed to pick contacts", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillDataFromContact(Intent data) {
        String name=null,phoneNo=null,email=null;
        ContentResolver contentResolver = getContentResolver();
        try {
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the contact ID
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            // column index of the contact name
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            cursor.close();
            // column index of the phone number
            Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            while (pCur.moveToNext()) {
                phoneNo = pCur.getString(
                        pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //print data
            }
            pCur.close();
            // column index of the email
            Cursor emailCur = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            while (emailCur.moveToNext()) {
                // This would allow you get several email addresses
                // if the email addresses were stored in an array
                email = emailCur.getString(
                        emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                //print data
            }
            emailCur.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (name != null){
            mEditName.setText(name);
        }
        if (email != null){
            mEditEmail.setText(email);
        }
        if (phoneNo != null){
            mEditContact.setText(phoneNo);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        pickContact();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
    }
}
