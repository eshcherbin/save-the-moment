package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveId;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;
import ru.spbau.savethemoment.datamanagers.DriveManager;
import ru.spbau.savethemoment.datamanagers.MediaChangeEventService;
import ru.spbau.savethemoment.datamanagers.MomentManager;

public class MomentViewActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<MomentViewActivity.MomentWithMediaContent>, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final String MOMENT_ID = "MomentId";

    private static final int LOADER_ID = 0;
    private static final int EDIT_MOMENT = 1;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 2;
    public static final String TAG = "MomentViewActivity";

    private Toolbar toolbar;
    private UUID momentId;
    private Moment moment;
    private List<DriveId> mediaContentDriveIds;
    private Menu menu;
    private MomentManager momentManager;
    private LinearLayout layoutMedia;
    private Context context;
    private ViewGroup mediaViewGroup;
    private BroadcastReceiver mediaChangeReceiver;

    private GoogleApiClient googleApiClient;
    private boolean hasDownloadedMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momentview);

        context = this;
        mediaViewGroup = (ViewGroup) findViewById(R.id.linearlayout_momentview_media);
        momentManager = new MomentManager(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
        hasDownloadedMedia = false;
        mediaChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DriveId driveId = intent.getParcelableExtra(MediaChangeEventService.DRIVE_ID);
                getLoaderManager().restartLoader(LOADER_ID, null, MomentViewActivity.this);
                displayMedia(driveId);
            }
        };
        registerReceiver(mediaChangeReceiver, new IntentFilter(MediaChangeEventService.ACTION));

        toolbar = (Toolbar) findViewById(R.id.tool_bar_momentview);

        momentId = (UUID) getIntent().getSerializableExtra(MOMENT_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mediaChangeReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (!hasDownloadedMedia && mediaContentDriveIds != null) {
            hasDownloadedMedia = true;
            for (DriveId driveId : mediaContentDriveIds) {
                displayMedia(driveId);
            }
        }
    }

    private void displayMedia(DriveId driveId) {
        DriveManager.loadFileContents(googleApiClient, driveId,
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        DriveContents driveContents = driveContentsResult.getDriveContents();
                        addPictureToLayout(BitmapFactory.decodeStream(driveContents.getInputStream()));
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(this, R.string.google_services_connection_failed_message, Toast.LENGTH_LONG).show();
            }
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0);
        }
    }

    @Override
    public Loader<MomentWithMediaContent> onCreateLoader(int id, Bundle args) {
        return new MomentLoader(this, momentId);
    }

    @Override
    public void onLoadFinished(Loader<MomentWithMediaContent> loader, MomentWithMediaContent data) {
        moment = data.moment;
        mediaContentDriveIds = data.mediaContentDriveIds;
        setSupportActionBar(toolbar);
        display();
    }

    @Override
    public void onLoaderReset(Loader<MomentWithMediaContent> loader) {
        moment = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_momentview, menu);
        showLocationButton();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_momentview_onmap) {
            Intent intent = new Intent(this, MapOfMomentsActivity.class);
            intent.putExtra(MapOfMomentsActivity.MOMENT, moment);
            intent.putExtra(MapOfMomentsActivity.IS_SINGLE_MOMENT, true);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menuitem_momentview_edit) {
            Intent intent = new Intent(this, MomentEditorActivity.class);
            intent.putExtra("Parent", "MomentView");
            intent.putExtra("Moment", moment);
            intent.putExtra("MediaContent", (Serializable) mediaContentDriveIds);
            startActivityForResult(intent, EDIT_MOMENT);
            return true;
        }
        if (id == R.id.menuitem_momentview_delete) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.alertdialog_delete_moment_title);
            alert.setMessage(R.string.alertdialog_delete_moment_text);
            alert.setPositiveButton(R.string.alertdialog_yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    momentManager.deleteMomentById(moment.getId());
                    new DriveManager.DeleteMomentFolderTask(getApplicationContext(), momentId).execute();
                    finish();
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton(R.string.alertdialog_no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_MOMENT && resultCode == Activity.RESULT_OK) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
            display();
            showLocationButton();
        } else {
            assert false : "MomentEditor didn't return a moment";
        }
    }

    private void showLocationButton() {
        MenuItem showOnMapItem = menu.findItem(R.id.menuitem_momentview_onmap);
        showOnMapItem.setVisible(moment.getLocation() != null);
    }

    private void display() {
        TextView title = (TextView) findViewById(R.id.textview_momentview_title);
        title.setText(moment.getTitle());

        TextView description = (TextView) findViewById(R.id.textview_momentview_description);
        description.setText(moment.getDescription());

        TextView location = (TextView) findViewById(R.id.textview_momentview_location);
        if (moment.getAddress() != null) {
            location.setText(moment.getAddress());
        } else {
            location.setText(null);
        }

        TextView capturingTime = (TextView) findViewById(R.id.textview_momentview_capturingtime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy hh:mm");
        capturingTime.setText(dateFormat.format(moment.getCapturingTime().getTime()));

        TagView tags = (TagView) findViewById(R.id.tagview_momentview_tags);
        tags.removeAll();
        Set<String> setOfTags = moment.getTags();
        List<Tag> listOfTags = new ArrayList<>();
        for (String tagText : setOfTags) {
            Tag tag = new Tag(tagText);
            tag.isDeletable = false;
            tag.layoutColorPress = tag.layoutColor;
            listOfTags.add(tag);
        }
        tags.addTags(listOfTags);
    }

    private void addPictureToLayout(Bitmap bitmap) {
        final View pictureItem = LayoutInflater.from(context).inflate(
                R.layout.momentview_picture_item, mediaViewGroup, false);
        ImageView image = (ImageView) pictureItem.findViewById(R.id.imageview_momentview_picture_item);
        image.setImageBitmap(getResizedBitmap(bitmap, layoutMedia.getWidth()));
        layoutMedia.addView(pictureItem);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float)height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static class MomentWithMediaContent {
        public Moment moment;
        public List<DriveId> mediaContentDriveIds;

        public MomentWithMediaContent(Moment moment, List<DriveId> mediaContentDriveIds) {
            this.moment = moment;
            this.mediaContentDriveIds = mediaContentDriveIds;
        }
    }

    private static class MomentLoader extends AsyncTaskLoader<MomentWithMediaContent> {
        private MomentManager momentManager;
        private UUID momentId;
        private Moment moment;
        private List<DriveId> mediaContentDriveIds;

        public MomentLoader(Context context, UUID momentId) {
            super(context);
            momentManager = new MomentManager(context);
            this.momentId = momentId;
        }

        @Override
        protected void onReset() {
            super.onReset();
            moment = null;
            mediaContentDriveIds = null;
        }

        @Override
        protected void onStartLoading() {
            if (takeContentChanged() || moment == null || mediaContentDriveIds == null) {
                forceLoad();
            } else {
                deliverResult(new MomentWithMediaContent(moment, mediaContentDriveIds));
            }
        }

        @Override
        public MomentWithMediaContent loadInBackground() {
            moment = momentManager.getMomentById(momentId);
            mediaContentDriveIds = momentManager.getMediaContentListByMomentId(momentId);
            return new MomentWithMediaContent(moment, mediaContentDriveIds);
        }
    }
}