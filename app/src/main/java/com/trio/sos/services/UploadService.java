package com.trio.sos.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.trio.sos.GoogleObjects;
import com.trio.sos.MainActivity;
import com.trio.sos.R;
import com.trio.sos.util.Constants;
import com.trio.sos.util.FileUtils;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UploadService extends IntentService {

    public static final String TAG = UploadService.class.getName();
    private static final String ACTION_UPLOAD = "com.trio.sos.services.action.UPLOAD";
    private static final String EXTRA_KEY_FILE_PATH = "com.trio.sos.action.FILE";
    public static final String INTENT_KEY_UPLOAD_FAILURE = "FAILURE";

    protected ResultReceiver mReceiver;

    private class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

        public FileUploadProgressListener() {
        }

        @Override
        public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
            if (mediaHttpUploader == null) return;
            switch (mediaHttpUploader.getUploadState()) {
                case INITIATION_STARTED:
                    Log.e(TAG, "Initiation has started!");
                    break;
                case INITIATION_COMPLETE:
                    Log.e(TAG, "Initiation is complete!");
                    break;
                case MEDIA_IN_PROGRESS:
                    double percent = mediaHttpUploader.getProgress() * 100;
                    Log.d(TAG, "Upload : " + " - " + String.valueOf(percent) + "%");
                    break;
                case MEDIA_COMPLETE:
                    Log.i(TAG, "Upload Complete");
            }
        }
    }

    public UploadService(){
        super(TAG);
    }

    public static void startActionUpload(@NonNull Context context, @NonNull String path) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_KEY_FILE_PATH, path);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                String path = intent.getStringExtra(EXTRA_KEY_FILE_PATH);
                handleUpload(path);
            }
        }
    }

    private void handleUpload(String path) {
        if (path == null){
            Log.i(TAG,"Null path Received as parameter");
            return;
        }
        GoogleObjects objects= (GoogleObjects) getApplicationContext();
        Drive mDriveService=objects.getDriveService();

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
            uploader.setProgressListener(new FileUploadProgressListener());
            File driveFile = insert.execute();
        } catch (UserRecoverableAuthIOException e) {
            e.printStackTrace();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deliverResultToReceiver(int resultCode, Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("INTENT",intent);
        mReceiver.send(resultCode, bundle);
    }
}
