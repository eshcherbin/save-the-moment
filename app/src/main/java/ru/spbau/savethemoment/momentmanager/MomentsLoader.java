package ru.spbau.savethemoment.momentmanager;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import java.util.Set;

public class MomentsLoader extends AsyncTaskLoader<Cursor> {
    private final MomentManager momentManager;
    private Cursor data;
    private Set<String> tags;
    private boolean locationRequired;

    public MomentsLoader(Context context, Set<String> tags, boolean locationRequired) {
        super(context);
        momentManager = new MomentManager(context);
        this.tags = tags;
        this.locationRequired = locationRequired;
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
            data = momentManager.getMoments(locationRequired);
        } else {
            data = momentManager.getMomentsByTags(tags, locationRequired);
        }
        return data;
    }
}
