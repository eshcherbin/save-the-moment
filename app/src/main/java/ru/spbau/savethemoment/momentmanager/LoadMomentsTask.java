package ru.spbau.savethemoment.momentmanager;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.spbau.savethemoment.common.Moment;

public class LoadMomentsTask extends AsyncTask<Set<String>, Void, List<Moment>> {
    private Context context;

    public LoadMomentsTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<Moment> doInBackground(Set<String>... params) {
        Set<String> tags = new HashSet<>();
        for (Set<String> tagsList : params) {
            tags.addAll(tagsList);
        }
        MomentManager momentManager = new MomentManager(context);
        Cursor cursor;
        if (tags.isEmpty()) {
            cursor = momentManager.getMoments();
        } else {
            cursor = momentManager.getMomentsByTags(tags);
        }
        List<Moment> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_ID));
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
            String address =
                    cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_ADDRESS));
            Set<String> momentTags = new HashSet<>();
            Cursor tagsCursor = momentManager.getTagsByMomentId(id);
            while (tagsCursor.moveToNext()) {
                momentTags.add(tagsCursor.getString(tagsCursor.getColumnIndexOrThrow(MomentManager.TAG_NAME)));
            }
            result.add(new Moment(id, title, description, capturingTime, location, address, momentTags));
        }
        return result;
    }
}
