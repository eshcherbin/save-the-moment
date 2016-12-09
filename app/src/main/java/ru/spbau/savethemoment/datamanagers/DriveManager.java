package ru.spbau.savethemoment.datamanagers;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.util.UUID;

public class DriveManager {

    public static DriveContents loadFileContents(GoogleApiClient googleApiClient, DriveId driveId) {
        DriveFile driveFile = driveId.asDriveFile();
        return driveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).await().getDriveContents();
    }

    public static void createMediaContentFile(GoogleApiClient googleApiClient, UUID momentId, DriveContents contents,
                                              ResultCallback<DriveFolder.DriveFileResult> resultCallback) {
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
        } else {
            momentFolder = buffer.get(0).getDriveId().asDriveFolder();
        }
        String fileTitle = UUID.randomUUID().toString();
        momentFolder.createFile(googleApiClient, new MetadataChangeSet.Builder().setTitle(fileTitle).build(), contents)
                    .setResultCallback(resultCallback);
    }
}
