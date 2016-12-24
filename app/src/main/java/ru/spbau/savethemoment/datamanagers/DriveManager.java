package ru.spbau.savethemoment.datamanagers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedOutputStream;
import java.util.UUID;

public class DriveManager {

    public static final String TAG = "DriveManager";

    public static void loadFileContents(GoogleApiClient googleApiClient, DriveId driveId,
                                        ResultCallback<DriveApi.DriveContentsResult> resultCallback) {
        DriveFile driveFile = driveId.asDriveFile();
        driveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(resultCallback);
    }

    public static void createMediaContentFile(GoogleApiClient googleApiClient, UUID momentId, DriveContents contents) {
        DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);
        MetadataBuffer buffer = appFolder.queryChildren(googleApiClient,
                new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                        momentId.toString()))
                        .build())
                .await().getMetadataBuffer();
        DriveFolder momentFolder;
        if (buffer.getCount() == 0) {
            momentFolder = appFolder.createFolder(googleApiClient,
                    new MetadataChangeSet.Builder().setTitle(momentId.toString()).build())
                    .await().getDriveFolder();
            if (momentFolder == null) {
                Log.e(TAG, "Could not create moment folder");
                return;
            }
        } else {
            momentFolder = buffer.get(0).getDriveId().asDriveFolder();
        }
        buffer.release();
        String fileTitle = UUID.randomUUID().toString();
        DriveFile newFile = momentFolder.createFile(googleApiClient, new MetadataChangeSet.Builder().setTitle(fileTitle).build(), contents)
                .await().getDriveFile();
        newFile.addChangeSubscription(googleApiClient).await();
    }

    public static void deleteMomentFolder(GoogleApiClient googleApiClient, UUID momentId) {
        DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);
        MetadataBuffer buffer = appFolder.queryChildren(googleApiClient,
                new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                        momentId.toString()))
                        .build())
                .await().getMetadataBuffer();
        if (buffer.getCount() > 0) {
            buffer.get(0).getDriveId().asDriveFolder().delete(googleApiClient).await();
        }
        buffer.release();
    }

    public static void deleteMediaContentFile(GoogleApiClient googleApiClient, DriveId driveId) {
        driveId.asDriveFile().delete(googleApiClient);
    }

    public static class UploadMediaTask extends ApiClientAsyncTask<Void, Void, Void> {
        public static final int COMPRESSION_QUALITY = 50;
        public static final String TAG = "UploadMediaTask";
        private UUID momentId;
        private Bitmap bitmap;

        public UploadMediaTask(Context context,
                               UUID momentId,
                               Bitmap bitmap) {
            super(context);
            this.momentId = momentId;
            this.bitmap = bitmap;
        }

        @Override
        protected Void doInBackgroundConnected(Void... params) {
            GoogleApiClient googleApiClient = getGoogleApiClient();
            Log.d(TAG, "started");
            Log.d(TAG, "googleApiClient.isConnected() == " + googleApiClient.isConnected());
            DriveContents driveContents = Drive.DriveApi.newDriveContents(googleApiClient).await().getDriveContents();
            Log.d(TAG, "got driveContents");
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY,
                    new BufferedOutputStream(driveContents.getOutputStream()));
            Log.d(TAG, "compressed bitmap");
            DriveManager.createMediaContentFile(googleApiClient, momentId, driveContents);
            Log.d(TAG, "completed");
            return null;
        }
    }

    public static class DeleteMomentFolderTask extends ApiClientAsyncTask<Void, Void, Void> {
        public static final String TAG = "DeleteMomentFolderTask";
        private UUID momentId;

        public DeleteMomentFolderTask(Context context,
                               UUID momentId) {
            super(context);
            this.momentId = momentId;
        }

        @Override
        protected Void doInBackgroundConnected(Void... params) {
            GoogleApiClient googleApiClient = getGoogleApiClient();
            Log.d(TAG, "started deleting moment folder");
            DriveManager.deleteMomentFolder(googleApiClient, momentId);
            Log.d(TAG, "completed");
            return null;
        }
    }
}
