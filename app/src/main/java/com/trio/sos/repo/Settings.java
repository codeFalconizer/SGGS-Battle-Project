package com.trio.sos.repo;

import android.content.Context;
import android.content.SharedPreferences;


public class Settings {
    //SharedPreference File name key
    private final static String SHARED_PREFERENCE_SETTINGS = "SETTINGS";

    //SharedPreference keys
    private final static String PREFERENCE_KEY_SMS = "SMS_ENABLE";
    private final static String PREFERENCE_KEY_EMAIL = "EMAIL_ENABLE";
    private final static String PREFERENCE_KEY_VIDEO = "VIDEO_ENABLE";
    private final static String PREFERENCE_KEY_VIDEO_DURATION = "VIDEO_DURATION";
    private final static String PREFERENCE_KEY_VIDEO_QUALITY = "VIDEO_QUALITY";

    //Instance variabless
    private Boolean videoAlert;
    private Boolean emailAlert;
    private Boolean smsAlert;
    private int videoDuration;
    private Boolean isvideoHQ;

    private Context mContext;

    public Settings(Context context) {
        mContext = context;
        update();
    }

    public void update() {
        SharedPreferences settings = mContext.getSharedPreferences(SHARED_PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        setEmailAlertEnabled(settings.getBoolean(PREFERENCE_KEY_EMAIL, true));
        setSmsAlertEnabled(settings.getBoolean(PREFERENCE_KEY_SMS, true));
        setVideoAlertEnabled(settings.getBoolean(PREFERENCE_KEY_VIDEO, true));
        setVideoDuration(settings.getInt(PREFERENCE_KEY_VIDEO_DURATION, 5));
        setVideoQualityHQ(settings.getBoolean(PREFERENCE_KEY_VIDEO_QUALITY, false));
    }

    //Method to be called after the installation of application
    public static void init(Context context) {
        Settings defaultSetting = new Settings(context);
        defaultSetting.save();
    }

    public void save() {
        SharedPreferences settings = mContext
                .getSharedPreferences(SHARED_PREFERENCE_SETTINGS
                        , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFERENCE_KEY_EMAIL, isEmailAlertEnabled());
        editor.putBoolean(PREFERENCE_KEY_SMS, isSmsAlertEnabled());
        editor.putBoolean(PREFERENCE_KEY_VIDEO, isVideoAlertEnabled());
        editor.putInt(PREFERENCE_KEY_VIDEO_DURATION, getVideoDuration());
        editor.putBoolean(PREFERENCE_KEY_VIDEO_QUALITY, isVideoHQ());
        editor.apply();
    }

    public void setVideoAlertEnabled(Boolean isVideoAlertEnabled) {
        this.videoAlert = isVideoAlertEnabled;
    }

    public void setEmailAlertEnabled(Boolean isEmailAlertEnabled) {
        this.emailAlert = isEmailAlertEnabled;
    }

    public void setSmsAlertEnabled(Boolean isSmsAlertEnabled) {
        this.smsAlert = isSmsAlertEnabled;
    }

    public Boolean isVideoAlertEnabled() {
        return videoAlert;
    }

    public Boolean isEmailAlertEnabled() {
        return emailAlert;
    }

    public Boolean isSmsAlertEnabled() {
        return smsAlert;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    public boolean isVideoHQ() {
        return isvideoHQ;
    }

    public void setVideoQualityHQ(boolean isvideoHQ) {
            this.isvideoHQ = isvideoHQ;
    }

    public void clear() {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SHARED_PREFERENCE_SETTINGS
                        , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
