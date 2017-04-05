package com.trio.sos;

import android.app.Application;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;

public class GoogleObjects extends Application {
    private GoogleAccountCredential mCredential;
    private Drive mDriveService;

    public GoogleAccountCredential getCredential() {
        return mCredential;
    }

    public void setCredential(GoogleAccountCredential credential) {
        this.mCredential = credential;
    }

    public Drive getDriveService() {
        return mDriveService;
    }

    public void setDriveService(Drive driveService) {
        this.mDriveService = driveService;
    }
}
