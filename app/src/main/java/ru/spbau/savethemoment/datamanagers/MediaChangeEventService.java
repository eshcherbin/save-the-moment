package ru.spbau.savethemoment.datamanagers;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEventService;

import java.util.UUID;

import ru.spbau.savethemoment.common.SaveTheMomentApplication;

public class MediaChangeEventService extends DriveEventService {

    public static final String TAG = "MediaChangeEventService";
    public static final String ACTION = "ru.spbau.savethemoment.MEDIA_ADDED";
    public static final String DRIVE_ID = "DriveId";

    @Override
    public void onChange(ChangeEvent changeEvent) {
        if (changeEvent.hasBeenDeleted()) {
            Log.d(TAG, "Oh, yet another file perished...");
            return;
        }
        Log.d(TAG, "Change event on board!");
        DriveId driveId = changeEvent.getDriveId();
        GoogleApiClient googleApiClient = SaveTheMomentApplication.getGoogleApiClient();
        DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);
        MetadataBuffer buffer = driveId.asDriveFile().listParents(googleApiClient).await().getMetadataBuffer();
        for (int i = 0; i < buffer.getCount(); i++) {
            Metadata metadata = buffer.get(i);
            DriveId parentId = metadata.getDriveId();
            if (parentId != appFolder) {
                MomentManager momentManager = new MomentManager(this);
                momentManager.insertMediaContent(UUID.fromString(metadata.getTitle()), driveId);
                // send broadcast to let MomentViewActivity know that a new media should be added
                Intent intent = new Intent();
                intent.setAction(ACTION);
                intent.putExtra(DRIVE_ID, driveId);
                sendBroadcast(intent);
            }
        }
        buffer.release();
    }
}

