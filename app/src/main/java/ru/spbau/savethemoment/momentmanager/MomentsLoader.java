package ru.spbau.savethemoment.momentmanager;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import java.util.Set;

public class MomentsLoader extends AsyncTaskLoader<Cursor> {
    private final MomentManager momentManager;
    private Cursor data;
    private Set<String> tags;

    public MomentsLoader(Context context, Set<String> tags) {
        super(context);
        momentManager = new MomentManager(context);
        this.tags = tags;
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
        if (tags == null) {
            data = momentManager.getMoments();
        } else {
            data = momentManager.getMomentsByTags(tags);
        }
        return data;
    }
}
