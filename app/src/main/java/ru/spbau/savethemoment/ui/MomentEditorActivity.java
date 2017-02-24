package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveId;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;
import ru.spbau.savethemoment.datamanagers.DriveManager;
import ru.spbau.savethemoment.datamanagers.MomentManager;

import static ru.spbau.savethemoment.R.string.alertdialog_tags_delete_text;
import static ru.spbau.savethemoment.R.string.error_title_required;

public class MomentEditorActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int CHOOSE_LOCATION_REQUEST_CODE = 0;
    private static final int CHOOSE_PICTURE_REQUEST_CODE = 1;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 2;

    private ViewGroup mediaViewGroup;
    private Toolbar toolbar;
    private Moment moment;
    private List<DriveId> initialMediaContentDriveIds;
    private EditText title;
    private EditText description;
    private EditText enterTag;
    private TextView date;
    private TextView time;
    private TextView location;
    private Button editDate;
    private Button editTime;
    private Button editLocation;
    private ImageButton addPicture;
    private LinearLayout layoutMedia;
    private TagView tags;
    private Context context;
    private boolean startedWithMoment;
    private boolean hasDownloadedMedia;

    private MomentManager momentManager;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momenteditor);

        context = this;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mediaViewGroup = (ViewGroup) findViewById(R.id.linearlayout_momenteditor_media);
        momentManager = new MomentManager(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
        hasDownloadedMedia = false;

        toolbar = (Toolbar) findViewById(R.id.tool_bar_momenteditor);
        setSupportActionBar(toolbar);

        startedWithMoment = getIntent().hasExtra("Moment");
        if (startedWithMoment) {
            moment = getIntent().getParcelableExtra("Moment");
            initialMediaContentDriveIds = (List<DriveId>) getIntent().getSerializableExtra("MediaContent");
        } else {
            moment = Moment.createCurrentMoment();
        }

        initTitle();
        initDescription();
        initDateAndTime();
        initLocation();
        initTags();
        initMedia();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_momenteditor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_momenteditor_save) {
            if (checkIfTitleEmpty()) {
                Toast.makeText(context, getResources().getString(error_title_required),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            saveTextChanges();
            Set<Bitmap> mediaToAdd = new HashSet<>();
            Set<DriveId> mediaNotToDelete = new HashSet<>();
            for (int i = 0; i < layoutMedia.getChildCount(); i++) {
                View pictureItem = layoutMedia.getChildAt(i);
                ImageView imageView;
                try {
                    imageView = (ImageView) pictureItem.findViewById(R.id.imageview_momenteditor_picture_item);
                } catch (ClassCastException e) {
                    continue;
                }
                DriveId driveId = (DriveId) pictureItem.getTag();
                if (driveId != null) {
                    mediaNotToDelete.add(driveId);
                } else {
                    mediaToAdd.add(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                }
            }
            if (startedWithMoment) {
                momentManager.updateMoment(moment);
                if (googleApiClient.isConnected()) {
                    if (initialMediaContentDriveIds != null) {
                        for (DriveId driveId : initialMediaContentDriveIds) {
                            if (!mediaNotToDelete.contains(driveId)) {
                                momentManager.deleteMediaContent(moment.getId(), driveId);
                                DriveManager.deleteMediaContentFile(googleApiClient, driveId);
                            }
                        }
                    }
                    for (Bitmap bitmap : mediaToAdd) {
                        DriveManager.uploadMediaContentFile(moment.getId(), bitmap);
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("Moment", moment);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                momentManager.insertMoment(moment);
                for (Bitmap bitmap : mediaToAdd) {
                    DriveManager.uploadMediaContentFile(moment.getId(), bitmap);
                }
                Intent intent = new Intent(this, MomentViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("MomentId", moment.getId());
                startActivity(intent);
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                LatLng latLng = data.getParcelableExtra(ChooseLocationActivity.POSITION_LAT_LNG_NAME);
                Location momentLocation = new Location("");
                momentLocation.setLatitude(latLng.latitude);
                momentLocation.setLongitude(latLng.longitude);
                moment.setLocation(momentLocation);
                String address = data.getStringExtra(ChooseLocationActivity.ADDRESS);
                location.setText(address);
                moment.setAddress(address);
            }
        }
        if (requestCode == CHOOSE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
                addPictureToLayout(BitmapFactory.decodeStream(inputStream), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == RESOLVE_CONNECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                googleApiClient.connect();
            } else {
                Toast.makeText(this, R.string.google_services_connection_failed_message, Toast.LENGTH_LONG).show();
            }
        }
    }

    boolean checkIfTitleEmpty() {
        return title.getText().toString().length() <= 0;
    }

    void setErrorOnTitle() {
        if (checkIfTitleEmpty()) {
            title.setError(getResources().getString(error_title_required));
        } else {
            title.setError(null);
        }
    }

    private void initTitle() {
        title = (EditText) findViewById(R.id.edittext_momenteditor_title);
        title.setText(moment.getTitle());
        setErrorOnTitle();
        title.addTextChangedListener(new TextWatcher()  {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s)  {
                setErrorOnTitle();
            }
        });
    }

    private void initDescription() {
        description = (EditText) findViewById(R.id.edittext_momenteditor_description);
        description.setText(moment.getDescription());
    }

    private void initDateAndTime() {
        date = (TextView) findViewById(R.id.textview_momenteditor_capturingtime_date);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy");
        date.setText(dateFormat.format(moment.getCapturingTime().getTime()));

        time = (TextView) findViewById(R.id.textview_momenteditor_capturingtime_time);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
        time.setText(timeFormat.format(moment.getCapturingTime().getTime()));

        editDate = (Button) findViewById(R.id.button_momenteditor_editdate);
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int curYear = calendar.get(Calendar.YEAR);
                int curMonth = calendar.get(Calendar.MONTH);
                int curDay = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                dateChanged(year, month, day, dateFormat);
                            }
                        }, curYear, curMonth, curDay);
                datePickerDialog.show();
            }
        });

        editTime = (Button) findViewById(R.id.button_momenteditor_edittime);
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int curHour = calendar.get(Calendar.HOUR_OF_DAY);
                int curMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                timeChanged(hour, minute, timeFormat);
                            }
                        }, curHour, curMinute, false);
                timePickerDialog.show();
            }
        });
    }

    private void initLocation() {
        location = (TextView) findViewById(R.id.textview_momenteditor_location);
        location.setText(moment.getAddress());

        editLocation = (Button) findViewById(R.id.button_momenteditor_editlocation);
        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChooseLocationActivity.class);
                Location currentMomentLocation = moment.getLocation();
                if (currentMomentLocation != null) {
                    intent.putExtra(ChooseLocationActivity.POSITION_LAT_LNG_NAME,
                            new LatLng(currentMomentLocation.getLatitude(),
                                    currentMomentLocation.getLongitude()));
                }
                startActivityForResult(intent, CHOOSE_LOCATION_REQUEST_CODE);
            }
        });
    }

    private void initTags() {
        tags = (TagView)findViewById(R.id.tagview_momenteditor_tags);
        displayTags();

        enterTag = (EditText) findViewById(R.id.edittext_momenteditor_tags_add);
        enterTag.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addTag();
                    return true;
                }
                return false;
            }
        });
    }

    private void initMedia() {
        layoutMedia = (LinearLayout) findViewById(R.id.linearlayout_momenteditor_media);

        addPicture = (ImageButton) findViewById(R.id.imagebutton_momenteditor_addpicture);
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: take a picture
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                startActivityForResult(pickIntent, CHOOSE_PICTURE_REQUEST_CODE);
            }
        });

        //TODO: add audio and video
    }

    private void saveTextChanges() {
        moment.setTitle(title.getText().toString());
        moment.setDescription(description.getText().toString());
    }

    private void dateChanged(int year, int month, int day, SimpleDateFormat dateFormat) {
        moment.setCapturingDate(year, month, day);
        date.setText(dateFormat.format(moment.getCapturingTime().getTime()));
    }

    private void timeChanged(int hour, int minute, SimpleDateFormat timeFormat) {
        moment.setCapturingTime(hour, minute);
        time.setText(timeFormat.format(moment.getCapturingTime().getTime()));
    }

    private void displayTags() {
        tags.removeAll();
        Set<String> setOfTags = moment.getTags();
        List<Tag> listOfTags = new ArrayList<>();
        for (String tagText : setOfTags) {
            listOfTags.add(tagFromString(tagText));
        }
        tags.addTags(listOfTags);
        tags.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(final Tag tag, final int i) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(R.string.alertdialog_tags_delete_title);
                alert.setMessage(getResources().getString(alertdialog_tags_delete_text) +
                        " \"" +
                        tag.text +
                        "\"?");
                alert.setPositiveButton(R.string.alertdialog_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeTag(tag, i);
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
        });
    }

    private void addTag() {
        String tagText = enterTag.getText().toString();
        moment.addTag(tagText);
        enterTag.setText("");
        tags.addTag(tagFromString(tagText));
    }

    private void removeTag(Tag tag, int i) {
        tags.remove(i);
        moment.deleteTag(tag.text);
    }

    private Tag tagFromString(String tagText) {
        Tag tag = new Tag(tagText);
        tag.isDeletable = true;
        return tag;
    }

    private void addPictureToLayout(Bitmap bitmap, DriveId driveId) {
        final View pictureItem = LayoutInflater.from(context).inflate(
                R.layout.momenteditor_picture_item, mediaViewGroup, false);
        ImageView image = (ImageView) pictureItem.findViewById(R.id.imageview_momenteditor_picture_item);
        image.setImageBitmap(getResizedBitmap(bitmap, layoutMedia.getWidth()));

        ImageButton button = (ImageButton) pictureItem.findViewById(R.id.imagebutton_momenteditor_picture_item);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(R.string.alertdialog_image_delete_title);
                alert.setMessage(R.string.alertdialog_image_delete_text);
                alert.setPositiveButton(R.string.alertdialog_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: save changes in Drive and DB
                        layoutMedia.removeView(pictureItem);
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
        });
        pictureItem.setTag(driveId);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!hasDownloadedMedia && initialMediaContentDriveIds != null) {
            hasDownloadedMedia = true;
            for (DriveId driveId : initialMediaContentDriveIds) {
                DriveManager.loadFileContents(googleApiClient, driveId,
                        new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                                DriveContents driveContents = driveContentsResult.getDriveContents();
                                addPictureToLayout(BitmapFactory.decodeStream(driveContents.getInputStream()),
                                        driveContents.getDriveId());
                            }
                        });
            }
        }
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
}
