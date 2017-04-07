package com.trio.sos.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.trio.sos.GoogleObjects;
import com.trio.sos.R;
import com.trio.sos.util.Constants;

import java.io.IOException;

public class UploadService extends IntentService {

    public static final String TAG = UploadService.class.getName();
    private static final String ACTION_UPLOAD = "UPLOAD";
    private static final String ACTION_CHANGE_PERMISSIONS = "PERMISSIONS";
    private static final String EXTRA_KEY_FILE_PATH = "FILE_PATH";
    private static final String EXTRA_KEY_AUTHORIZATION_RECEIVER = "AUTHORIZATION_RECEIVER";

    protected ResultReceiver mReceiver;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final int NOTIFICATION_ID = 1;
    private File driveFile;

    private class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

        private FileUploadProgressListener() {}

        @Override
        public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
            if (mediaHttpUploader == null) return;
            switch (mediaHttpUploader.getUploadState()) {
                case INITIATION_STARTED:
                    Log.e(TAG, "Initiation has started!");
                    sendNotification(-1);
                    break;
                case INITIATION_COMPLETE:
                    Log.e(TAG, "Initiation is complete!");
                    sendNotification(0);
                    break;
                case MEDIA_IN_PROGRESS:
                    double percent = mediaHttpUploader.getProgress() * 100;
                    Log.d(TAG, "Upload : " + String.valueOf((int) percent) + " %");
                    sendNotification((int)percent);
                    break;
                case MEDIA_COMPLETE:
                    Log.i(TAG, "Upload Complete");
                    sendNotification(101);
            }
        }
    }

    public UploadService() {
        super(TAG);
    }

    public static void startActionUpload(@NonNull Context context
            , @NonNull Bundle bundle) {
        String path = bundle.getString(Constants.BUNDLE_KEY_FILE_PATH);
        ResultReceiver receiver = bundle.getParcelable(Constants.BUNDLE_KEY_AUTHORIZATION_RECEIVER);
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_KEY_AUTHORIZATION_RECEIVER, receiver);
        intent.putExtra(EXTRA_KEY_FILE_PATH, path);
        context.startService(intent);
    }

    public static void startActionChangePermission(@NonNull Context context){
        Intent intent = new Intent(context,UploadService.class);
        intent.setAction(ACTION_CHANGE_PERMISSIONS);
        context.startService(intent);
    }

    private void handlePermissionChange() {
        String fileId = driveFile.getId();
        Drive mService = ((GoogleObjects)getApplicationContext()).getDriveService();
        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onFailure(GoogleJsonError e,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                // Handle error
                Log.e(TAG,e.getMessage());
            }

            @Override
            public void onSuccess(Permission permission,
                                  HttpHeaders responseHeaders)
                    throws IOException {
            }
        };
        try {
            BatchRequest batch = mService.batch();
            Permission userPermission = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            mService.permissions().insert(fileId, userPermission)
                    .setFields("id")
                    .queue(batch, callback);

            Permission domainPermission = new Permission()
                    .setType("domain")
                    .setRole("reader")
                    .setValue("example.com");
            mService.permissions().insert(fileId, domainPermission)
                    .setFields("id")
                    .queue(batch, callback);
            batch.execute();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BUNDLE_KEY_ALTERNATE_LINK,driveFile.getAlternateLink());
            deliverResultToReceiver(Constants.SUCCESS,bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                ResultReceiver receiver = intent.getParcelableExtra(EXTRA_KEY_AUTHORIZATION_RECEIVER);
                String path = intent.getStringExtra(EXTRA_KEY_FILE_PATH);
                handleUpload(path, receiver);
            }else if (ACTION_CHANGE_PERMISSIONS.equals(action)){
                handlePermissionChange();
            }
        }
    }

    private void handleUpload(@NonNull String path, @NonNull ResultReceiver receiver) {
        mReceiver = receiver;
        GoogleObjects objects = (GoogleObjects) getApplicationContext();
        Drive mDriveService = objects.getDriveService();

        File metaData = new File();
        metaData.setTitle("video_" + (int) (Math.random() * 10000000) + ".mp4");
        metaData.setMimeType(Constants.MIME_VIDEO);
        metaData.setDescription("Emergency Video sent from " + getResources().getString(R.string.app_name));
        try {
            java.io.File file =
                    new java.io.File(path);
            FileContent mediaContent = new FileContent(Constants.MIME_VIDEO, file);
            Drive.Files.Insert insert = mDriveService.files().insert(metaData, mediaContent);
            MediaHttpUploader uploader = insert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);
            uploader.setChunkSize(1024 * 1024);
            createNotification();
            uploader.setProgressListener(new FileUploadProgressListener());
            File driveFile = insert.execute();
            this.driveFile = driveFile;
        } catch (UserRecoverableAuthIOException e) {
            e.printStackTrace();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_KEY_INTENT,e.getIntent());
            deliverResultToReceiver(Constants.FAILURE, bundle);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deliverResultToReceiver(int resultCode, Bundle bundle) {
        mReceiver.send(resultCode, bundle);
    }

    private void createNotification(){
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mBuilder.setContentTitle(this.getResources().getString(R.string.app_name));
        mBuilder.setContentText("Beginning Upload to Google Drive");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
    }

    private void sendNotification(int progress){
        switch(progress){
            case -1:
                mBuilder.setProgress(0,0,true);
                break;
            case 0:
                mBuilder.setProgress(100,0,false);
                break;
            case 101:
                mBuilder.setProgress(0,0,false);
                mBuilder.setContentText("Upload Complete");
                break;
            default:
                mBuilder.setContentText("Uploading file to Google Drive "+progress+" %");
                mBuilder.setProgress(100,progress,false);
                mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        }
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
