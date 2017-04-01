package com.trio.sos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.trio.sos.helper.Constants;
import com.trio.sos.repo.LoggedUser;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


public class InfoActivity extends Activity{

    public static final String TAG = InfoActivity.class.getName();

    EditText mEditName,mEditContact,mEditEmail,mEditDob;
    Button mSubmitButton;
    RadioGroup mRadioGroup;
    Intent mData;
    CircleImageView mProfilePicture;
    ImageButton mDob ;
    Dialog mDatePickerDialog;
    LoggedUser mUser;
    boolean mRadioFlag = false;

    private class LoadImage extends AsyncTask<Uri,Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(Uri... params) {
            String url = params[0].toString();
            Bitmap bitmap=null;
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return  bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null){
                mProfilePicture.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        mData=getIntent();
        bindActivity();
        Log.i(TAG,mData.getStringExtra(Constants.KEY_INTENT_FROM));
        if (mData.getStringExtra(Constants.KEY_INTENT_FROM).equals(LoginActivity.TAG)){
            fillDataFromIntent();
            mUser = new LoggedUser(this);
        }else if(mData.getStringExtra(Constants.KEY_INTENT_FROM).equals(MainActivity.TAG)){
            fillDataFromLoggedUser();
        }

    }
    private void fillDataFromIntent(){
        try{
            mEditName.setText(mData.getStringExtra(Constants.KEY_NAME));
            mEditEmail.setText(mData.getStringExtra(Constants.KEY_EMAIL));
            Uri uri = mData.getParcelableExtra(Constants.KEY_PHOTO_URL);
            if (uri.toString() != null){
                new LoadImage().execute(uri);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void fillDataFromLoggedUser(){
        LoggedUser user = new LoggedUser(this);
        mEditName.setText(user.getName());
        mEditEmail.setText(user.getEmail());
        mEditDob.setText(user.getDob());
        mEditContact.setText(user.getContactNo());
        if (user.getGender().equals(Constants.GENDER_MALE)){
            mRadioGroup.check(R.id.info_radio_button_male);
        }else{
            mRadioGroup.check(R.id.info_radio_button_female);
        }
        mEditName.setEnabled(false);
        mEditEmail.setEnabled(false);
        mEditDob.setEnabled(false);
        mRadioGroup.setEnabled(false);
        mEditContact.setEnabled(false);
        mSubmitButton.setVisibility(Button.GONE);
    }

    private void bindActivity(){
        //binding activity to views
        mEditName= (EditText) findViewById(R.id.info_edit_name);
        mEditContact= (EditText) findViewById(R.id.info_edit_contact);
        mEditEmail= (EditText) findViewById(R.id.info_edit_email);
        mProfilePicture = (CircleImageView) findViewById(R.id.info_profilePicture);
        mDob = (ImageButton) findViewById(R.id.info_button_dob);
        mEditDob = (EditText) findViewById(R.id.info_edit_dob);
        mSubmitButton = (Button) findViewById(R.id.info_button_submit);
        mRadioGroup  =  (RadioGroup) findViewById(R.id.info_radio_group);


        //Setting Listeners
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.info_radio_button_male){
                    mUser.setGender(Constants.GENDER_MALE);
                }else{
                    mUser.setGender(Constants.GENDER_FEMALE);
                }
                mRadioFlag=true;
            }
        });

        mDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    submit();
                }
            }
        });

        DatePicker datePicker = new DatePicker(this);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                mEditDob.setText(""+dayOfMonth+"/ "+(month+1)+"/ "+year);
                mDatePickerDialog.dismiss();
            }
        });
        mDatePickerDialog =new Dialog(this);
        mDatePickerDialog.setTitle("Choose your Date of Birth");
        ViewGroup.LayoutParams layoutParams =new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mDatePickerDialog.addContentView(datePicker,layoutParams);
    }

    private boolean validate(){
        boolean flag = true;
        if (mEditName.getText().toString().length() == 0 ){
            mEditName.setError("Name Required");
            flag=false;
        }else if (mEditContact.getText().toString().length() == 0 ){
            mEditContact.setError("Contact No. Required");
            flag=false;
        }else if (mEditDob.getText().toString().length() == 0 ){
            mEditDob.setError("Cannot be left empty");
            flag=false;
        }else if (mEditEmail.getText().toString().length() == 0 ){
            mEditEmail.setError("Email Required");
            flag=false;
        }else if (!mRadioFlag){
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        return flag;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void submit(){
        mUser.setName(mEditName.getText().toString());
        mUser.setEmail(mEditEmail.getText().toString());
        mUser.setContactNo(mEditContact.getText().toString());
        mUser.setDob(mEditDob.getText().toString());
        if (mData.getParcelableExtra(Constants.KEY_PHOTO_URL) != null){
            mUser.setPhotoUrl(mData.getParcelableExtra(Constants.KEY_PHOTO_URL).toString());
        }else {
            mUser.setPhotoUrl(null);
        }
        mUser.save();
        SharedPreferences route = getSharedPreferences(
                Constants.SHARED_PREFERENCE_ROUTE,MODE_PRIVATE);
        SharedPreferences.Editor edit = route.edit();
        edit.putBoolean(Constants.SHARED_PREFERENCE_KEY_ROUTE,true);
        edit.apply();
        Intent intent = new Intent(this,ContactsActivity.class);
        intent.putExtra(Constants.KEY_INTENT_FROM,TAG);
        startActivity(intent);
        finish();
    }
}
