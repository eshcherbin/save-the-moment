package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;
import ru.spbau.savethemoment.momentmanager.MomentManager;

public class MomentViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Moment> {

    private static final int LOADER_ID = 0;
    private static final int EDIT_MOMENT = 1;
    private Toolbar toolbar;
    private UUID momentId;
    private Moment moment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momentview);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_momentview);

        momentId = (UUID) getIntent().getSerializableExtra("MomentId");
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Moment> onCreateLoader(int id, Bundle args) {
        return new MomentLoader(this, momentId);
    }

    @Override
    public void onLoadFinished(Loader<Moment> loader, Moment data) {
        moment = data;
        setSupportActionBar(toolbar);
        display();
    }

    @Override
    public void onLoaderReset(Loader<Moment> loader) {
        moment = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_momentview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_momentview_onmap) {
            //TODO: show moment on map
            return true;
        }
        if (id == R.id.menuitem_momentview_edit) {
            Intent intent = new Intent(this, MomentEditorActivity.class);
            intent.putExtra("Parent", "MomentView");
            intent.putExtra("Moment", moment);
            startActivityForResult(intent, EDIT_MOMENT);
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_MOMENT && resultCode == Activity.RESULT_OK) {
            moment = (Moment) data.getSerializableExtra("Moment");
            display();
        } else {
            assert false : "MomentEditor didn't return a moment";
        }
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

        //TODO: displaying media content
    }

    private static class MomentLoader extends AsyncTaskLoader<Moment> {
        private MomentManager momentManager;
        private UUID momentId;
        private Moment data;

        public MomentLoader(Context context, UUID momentId) {
            super(context);
            momentManager = new MomentManager(context);
            this.momentId = momentId;
        }

        @Override
        protected void onReset() {
            super.onReset();
            data = null;
        }

        @Override
        protected void onStartLoading() {
            if (takeContentChanged() || data == null) {
                forceLoad();
            } else {
                deliverResult(data);
            }
        }

        @Override
        public Moment loadInBackground() {
            Cursor cursor = momentManager.getMomentById(momentId);
            cursor.moveToFirst();
            String title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_TITLE));
            String description =
                    cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_DESCRIPTION));
            Calendar capturingTime = new GregorianCalendar();
            capturingTime.setTimeInMillis(cursor.getLong(
                    cursor.getColumnIndexOrThrow(MomentManager.MOMENT_CAPTURING_TIME)));
            Location location = new Location("");
            location.setLongitude(cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MomentManager.MOMENT_LOCATION_LONGITUDE)));
            location.setLatitude(cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MomentManager.MOMENT_LOCATION_LATITUDE)));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_ADDRESS));
            Set<String> momentTags = new HashSet<>();
            Cursor tagsCursor = momentManager.getTagsByMomentId(momentId);
            while (tagsCursor.moveToNext()) {
                momentTags.add(tagsCursor.getString(tagsCursor.getColumnIndexOrThrow(MomentManager.TAG_NAME)));
            }
            data = new Moment(momentId, title, description, capturingTime, location, address, momentTags);
            return data;
        }
    }
}