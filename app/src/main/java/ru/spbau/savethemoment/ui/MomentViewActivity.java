package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.UUID;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;
import ru.spbau.savethemoment.momentmanager.MomentManager;

public class MomentViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Moment> {

    public static final String MOMENT_ID = "MomentId";

    private static final int LOADER_ID = 0;
    private static final int EDIT_MOMENT = 1;
    private Toolbar toolbar;
    private UUID momentId;
    private Moment moment;
    private Menu menu;
    private MomentManager momentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momentview);

        momentManager = new MomentManager(this);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_momentview);

        momentId = (UUID) getIntent().getSerializableExtra(MOMENT_ID);
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
            moment = data.getParcelableExtra("Moment");
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

        TextView tags = (TextView) findViewById(R.id.textview_momentview_tags);
        StringBuilder stringBuilder = new StringBuilder();
        for (String tag : moment.getTags()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(tag);
        }
        tags.setText(stringBuilder.toString());
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
            data = momentManager.getMomentById(momentId);
            return data;
        }
    }
}