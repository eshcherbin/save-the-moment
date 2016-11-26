package ru.spbau.savethemoment.ui;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.momentmanager.MomentManager;

public class ListOfMomentsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;

    private Toolbar toolbar;
    private ListView listViewMoments;
    private ListOfMomentsAdapter listOfMomentsAdapter;

    public void onFilterButtonClicked(View view) {
        //TODO: implement filtering
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_moments);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_list_of_moments);
        setSupportActionBar(toolbar);

        listViewMoments = (ListView) findViewById(R.id.listview_list_of_moments);
        listViewMoments.setEmptyView(findViewById(R.id.text_list_of_moments_empty));
        listOfMomentsAdapter = new ListOfMomentsAdapter(this, null, 0);
        listViewMoments.setAdapter(listOfMomentsAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        listViewMoments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: start MomentViewActivity
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_listofmoments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_list_of_moments_new_moment) {
            Intent intent = new Intent(this, MomentEditorActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MomentsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listOfMomentsAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listOfMomentsAdapter.changeCursor(null);
    }

    private static class ListOfMomentsAdapter extends CursorAdapter {
        private static final String DATETIME_FORMAT = "HH:mm dd.MM.yyyy";

        public ListOfMomentsAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.listview_list_of_moments_item, parent, false);
            ViewHolder viewHolder =
                    new ViewHolder((TextView) view.findViewById(R.id.text_list_of_moments_item_title),
                                   (TextView) view.findViewById(R.id.text_list_of_moments_item_datetime));
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            TextView titleView = viewHolder.titleView;
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_TITLE));
            titleView.setText(title);

            long capturingTimeInMillis =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_CAPTURING_TIME));
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT);
            TextView capturingTimeView = viewHolder.capturingTimeView;
            capturingTimeView.setText(dateFormat.format(capturingTimeInMillis));
        }

        private static class ViewHolder {
            public final TextView titleView;
            public final TextView capturingTimeView;

            public ViewHolder(TextView titleView, TextView capturingTimeView) {
                this.titleView = titleView;
                this.capturingTimeView = capturingTimeView;
            }

        }
    }

    private static class MomentsLoader extends AsyncTaskLoader<Cursor> {
        private final MomentManager momentManager;
        private Cursor data;

        public MomentsLoader(Context context) {
            super(context);
            momentManager = new MomentManager(context);
        }

        @Override
        protected void onReset() {
            super.onReset();
            if (data != null) {
                data.close();
            }
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
        public Cursor loadInBackground() {
            data = momentManager.getMoments();
            return data;
        }
    }
}

