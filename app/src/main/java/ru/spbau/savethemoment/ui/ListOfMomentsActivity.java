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

import ru.spbau.savethemoment.R;

public class ListOfMomentsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object> {

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
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return new
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    private static class ListOfMomentsAdapter extends CursorAdapter {
        public ListOfMomentsAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.listview_list_of_moments_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }
    }

    private static class MomentsLoader extends AsyncTaskLoader<Cursor> {

        public MomentsLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            return null;
        }
    }
}

