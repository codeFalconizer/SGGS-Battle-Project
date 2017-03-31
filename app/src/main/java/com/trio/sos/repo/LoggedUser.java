package com.trio.sos.repo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Pranav on 31-03-2017.
 */

public class LoggedUser {
    private static String SHARED_PREFERENCE_PROFILE = "USER_PROFILE";
    private static String PREFERENCE_KEY_NAME = "NAME";
    private static String PREFERENCE_KEY_CONTACT_NUMBER = "CONTACT_NO";
    private static String PREFERENCE_KEY_EMAIL = "EMAIL";
    private static String PREFERENCE_KEY_GENDER = "GENDER";
    private static String PREFERENCE_KEY_DOB = "DOB";
    private static String PREFERENCE_KEY_PHOTO_URL = "PHOTO_URL";


    private String name;
    private String email;
    private String contact_no;
    private String gender;
    private String dob;
    private String photoUrl;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context mContext;

    public LoggedUser(Context context) {
        mContext = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_PROFILE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setName(sharedPreferences.getString(PREFERENCE_KEY_NAME, ""));
        setEmail(sharedPreferences.getString(PREFERENCE_KEY_EMAIL, ""));
        setContactNo(sharedPreferences.getString(PREFERENCE_KEY_CONTACT_NUMBER, ""));
        setGender(sharedPreferences.getString(PREFERENCE_KEY_GENDER, ""));
        setDob(sharedPreferences.getString(PREFERENCE_KEY_DOB, ""));
        setPhotoUrl(sharedPreferences.getString(PREFERENCE_KEY_PHOTO_URL, ""));
    }

    public void save() {
        editor.putString(PREFERENCE_KEY_NAME, getName());
        editor.putString(PREFERENCE_KEY_CONTACT_NUMBER, getContactNo());
        editor.putString(PREFERENCE_KEY_EMAIL, getEmail());
        editor.putString(PREFERENCE_KEY_GENDER, getGender());
        editor.putString(PREFERENCE_KEY_DOB, getDob());
        editor.putString(PREFERENCE_KEY_PHOTO_URL, getPhotoUrl());
        editor.apply();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNo() {
        return contact_no;
    }

    public void setContactNo(String contact_no) {
        this.contact_no = contact_no;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void clear(){
        editor.clear();
        editor.commit();
    }

}
