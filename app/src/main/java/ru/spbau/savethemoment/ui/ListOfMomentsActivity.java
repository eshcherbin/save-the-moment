package ru.spbau.savethemoment.ui;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.momentmanager.MomentManager;

public class ListOfMomentsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;

    private ListView listViewMoments;
    private ListOfMomentsAdapter listOfMomentsAdapter;

    public void onFilterButtonClicked(View view) {
        //TODO: implement filtering
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_moments);

        listViewMoments = (ListView) findViewById(R.id.listview_list_of_moments);
        listOfMomentsAdapter = new ListOfMomentsAdapter(this, null, 0);
        listViewMoments.setAdapter(listOfMomentsAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
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
            return LayoutInflater.from(context).inflate(R.layout.listview_list_of_moments_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView itemName = (TextView) view.findViewById(R.id.text_list_of_moments_item_title);
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_TITLE));
            itemName.setText(title);

            long capturingTimeInMillis =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_CAPTURING_TIME));
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT);
            TextView itemDatetime = (TextView) view.findViewById(R.id.text_list_of_moments_item_datetime);
            itemDatetime.setText(dateFormat.format(capturingTimeInMillis));

            view.setTag(cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_ID)));
        }
    }

    private static class MomentsLoader extends AsyncTaskLoader<Cursor> {
        public MomentsLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            MomentManager momentManager = new MomentManager(getContext());
            return momentManager.getMoments();
        }
    }
}

