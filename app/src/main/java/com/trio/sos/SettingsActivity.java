package com.trio.sos;

import android.Manifest;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.trio.sos.util.Constants;
import com.trio.sos.repo.Settings;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener,EasyPermissions.PermissionCallbacks {

    Switch mSmsSwitch,mVideoSwitch,mEmailSwitch,mQualitySwitch;
    Settings mSettings;
    Spinner mVideoDurationSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Getting Settings object
        mSettings = new Settings(this);

        //Attaching Views
        mSmsSwitch = (Switch) findViewById(R.id.setting_switch_sms);
        mEmailSwitch = (Switch) findViewById(R.id.setting_switch_email);
        mVideoSwitch = (Switch) findViewById(R.id.setting_switch_video);
        mVideoDurationSpinner = (Spinner) findViewById(R.id.setting_spinner_video_duration);
        mQualitySwitch = (Switch)findViewById(R.id.setting_switch_quality);

        //Setting Listeners
        mSmsSwitch.setOnClickListener(this);
        mEmailSwitch.setOnClickListener(this);
        mVideoSwitch.setOnClickListener(this);
        mVideoDurationSpinner.setOnItemSelectedListener(this);
        mQualitySwitch.setOnClickListener(this);

        //Getting settings
        mSmsSwitch.setChecked(mSettings.isSmsAlertEnabled());
        mEmailSwitch.setChecked(mSettings.isEmailAlertEnabled());
        mVideoSwitch.setChecked(mSettings.isVideoAlertEnabled());
        mQualitySwitch.setChecked(mSettings.isVideoHQ());
        if (!mVideoSwitch.isChecked()){
            mVideoDurationSpinner.setEnabled(false);
            mQualitySwitch.setEnabled(false);
        }

        ArrayList<Integer> list = new ArrayList<>();
        int[] array = getResources().getIntArray(R.array.setting_video_duration);
        for (int i : array){
            list.add(i);
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mVideoDurationSpinner.setAdapter(arrayAdapter);
        mVideoDurationSpinner.setSelection(mSettings.getVideoDuration()/5-1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void checkPermissions(){
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)){
            mSmsSwitch.setChecked(false);
            mSettings.setSmsAlertEnabled(false);
            if (!mSettings.isEmailAlertEnabled()){
                mSettings.setSmsAlertEnabled(true);
                mEmailSwitch.setChecked(true);
            }
            mSettings.save();
        }
    }

    private AlertDialog constructDialogs(){
        AlertDialog.Builder video = new AlertDialog.Builder(this);
        video.setCancelable(false);
        video.setMessage(getResources().getString(R.string.setting_alert_message_quality));
        video.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSettings.setVideoQualityHQ(true);
                mSettings.save();
            }
        });
        video.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mSettings.setVideoQualityHQ(false);
                mQualitySwitch.setChecked(false);
                mSettings.save();
            }
        });
        return video.create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setting_switch_sms:
                if (!mSmsSwitch.isChecked()){
                    if (!mEmailSwitch.isChecked()){
                        mSmsSwitch.setChecked(true);
                        Toast.makeText(this, "Both Email and SMS alert cannot be turned off", Toast.LENGTH_SHORT).show();
                    }else{
                        mSettings.setSmsAlertEnabled(false);
                    }
                }else{
                    if (EasyPermissions.hasPermissions(this,Manifest.permission.SEND_SMS)){
                        mSettings.setSmsAlertEnabled(true);
                    }else {
                        EasyPermissions.requestPermissions(this
                                ,"Application needs SMS permission to send SMS to emergency contacts"
                        , Constants.REQUEST_PERMISSION_SEND_SMS,Manifest.permission.SEND_SMS);
                    }
                }
                break;
            case R.id.setting_switch_email:
                if (!mEmailSwitch.isChecked()){
                    if (!mSmsSwitch.isChecked()){
                        mEmailSwitch.setChecked(true);
                        Toast.makeText(this, "Both Email and SMS alert cannot be turned off", Toast.LENGTH_SHORT).show();
                    }else{
                        mSettings.setEmailAlertEnabled(false);
                    }
                }else{
                    mSettings.setEmailAlertEnabled(true);
                }
                break;
            case R.id.setting_switch_video:
                if (mVideoSwitch.isChecked()){
                    mVideoDurationSpinner.setEnabled(true);
                    mQualitySwitch.setEnabled(true);
                    mSettings.setVideoAlertEnabled(true);
                }else{
                    mVideoDurationSpinner.setEnabled(false);
                    mQualitySwitch.setEnabled(false);
                    mSettings.setVideoAlertEnabled(false);
                }
                break;
            case R.id.setting_switch_quality:
                if (mQualitySwitch.isChecked()){
                    AlertDialog alert = constructDialogs();
                    alert.show();
                }else{
                    mSettings.setVideoQualityHQ(false);
                }
        }
        mSettings.save();
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSettings.setVideoDuration((Integer) parent.getItemAtPosition(position));
        mVideoDurationSpinner.setSelection(mSettings.getVideoDuration()/5-1);
        mSettings.save();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mSettings.setVideoDuration(10);
        mSettings.save();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode==Constants.REQUEST_PERMISSION_SEND_SMS) {
            mSettings.setSmsAlertEnabled(true);
            mSettings.save();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        mSmsSwitch.setChecked(false);
        Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
    }
}
