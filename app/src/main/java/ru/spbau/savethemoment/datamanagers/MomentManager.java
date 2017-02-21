package ru.spbau.savethemoment.datamanagers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import com.google.android.gms.drive.DriveId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ru.spbau.savethemoment.common.Moment;

public class MomentManager {

    private static final String DB_NAME = "moments_db";
    private static final int DB_VERSION = 1;

    private static final String MOMENTS_TABLE = "moments";
    private static final String TAGS_TABLE = "tags";
    private static final String MEDIA_TABLE = "media";

    public static final String MOMENT_ID = "_id";
    public static final String MOMENT_TITLE = "title";
    public static final String MOMENT_DESCRIPTION = "description";
    public static final String MOMENT_CAPTURING_TIME = "capturing_time";
    public static final String MOMENT_LOCATION_LONGITUDE = "location_longitude";
    public static final String MOMENT_LOCATION_LATITUDE = "location_latitude";
    public static final String MOMENT_ADDRESS = "address";

    public static final String TAG_MOMENT_ID = "moment_id";
    public static final String TAG_NAME = "name";
    public static final String TAGS = "Tags";

    public static final String MEDIA_MOMENT_ID = "moment_id";
    public static final String MEDIA_DRIVE_ID = "drive_id";

    private static final String[] MOMENT_COLUMNS = new String[]{MOMENT_ID, MOMENT_TITLE,
            MOMENT_DESCRIPTION, MOMENT_CAPTURING_TIME,
            MOMENT_LOCATION_LONGITUDE, MOMENT_LOCATION_LATITUDE, MOMENT_ADDRESS};

    private static final String DB_MOMENTS_CREATE = "create table " + MOMENTS_TABLE + "("
            + MOMENT_ID + " text primary key not null, "
            + MOMENT_TITLE + " text not null, "
            + MOMENT_DESCRIPTION + " text, "
            + MOMENT_CAPTURING_TIME + " integer not null, "
            + MOMENT_LOCATION_LONGITUDE + " double, "
            + MOMENT_LOCATION_LATITUDE + " double, "
            + MOMENT_ADDRESS + " text " + ");";
    private static final String DB_TAGS_CREATE = "create table " + TAGS_TABLE + "("
            + TAG_MOMENT_ID + " text not null, "
            + TAG_NAME + " text not null, "
            + "foreign key(" + TAG_MOMENT_ID + ") references " + MOMENTS_TABLE + "(" + MOMENT_ID + ") on delete cascade"
            + ");";
    private static final String DB_MEDIA_CREATE = "create table " + MEDIA_TABLE + "("
            + MEDIA_MOMENT_ID + " text not null, "
            + MEDIA_DRIVE_ID + " text not null, "
            + "foreign key(" + MEDIA_MOMENT_ID
            + ") references " + MOMENTS_TABLE + "(" + MOMENT_ID + ") on delete cascade"
            + ");";
    public static final String TAG = "MomentManager";

    private DBHelper dbHelper;

    public MomentManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public Cursor getMoments(boolean locationRequired) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        return database.query(MOMENTS_TABLE, MOMENT_COLUMNS,
                locationRequired
                        ? MOMENT_LOCATION_LONGITUDE + " is not null and " + MOMENT_LOCATION_LATITUDE + " is not null"
                        : null,
                null, null, null, null);
    }

    public Moment getMomentById(UUID momentId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        database.beginTransaction();
        Cursor momentCursor = database.query(MOMENTS_TABLE, MOMENT_COLUMNS, MOMENT_ID + "=?",
                new String[]{momentId.toString()}, null, null, null);
        if (momentCursor.moveToFirst()) {
            String title =
                    momentCursor.getString(momentCursor.getColumnIndexOrThrow(MomentManager.MOMENT_TITLE));
            int descriptionColumnIndex = momentCursor.getColumnIndexOrThrow(MomentManager.MOMENT_DESCRIPTION);
            String description = momentCursor.isNull(descriptionColumnIndex) ? null
                    : momentCursor.getString(descriptionColumnIndex);
            Calendar capturingTime = new GregorianCalendar();
            capturingTime.setTimeInMillis(momentCursor.getLong(
                    momentCursor.getColumnIndexOrThrow(MomentManager.MOMENT_CAPTURING_TIME)));
            int locationLongitudeColumnIndex =
                    momentCursor.getColumnIndexOrThrow(MomentManager.MOMENT_LOCATION_LONGITUDE);
            int locationLatitudeColumnIndex =
                    momentCursor.getColumnIndexOrThrow(MomentManager.MOMENT_LOCATION_LATITUDE);
            Location location;
            if (momentCursor.isNull(locationLatitudeColumnIndex) ||
                    momentCursor.isNull(locationLongitudeColumnIndex)) {
                location = null;
            } else {
                location = new Location("");
                location.setLongitude(momentCursor.getDouble(locationLongitudeColumnIndex));
                location.setLatitude(momentCursor.getDouble(locationLatitudeColumnIndex));
            }
            int addressColumnIndex = momentCursor.getColumnIndexOrThrow(MOMENT_ADDRESS);
            String address = momentCursor.isNull(addressColumnIndex) ? null
                    : momentCursor.getString(addressColumnIndex);
            Set<String> momentTags = getTagsByMomentId(momentId);
            database.setTransactionSuccessful();
            database.endTransaction();
            return new Moment(momentId, title, description, capturingTime, location, address, momentTags);
        } else {
            return null;
        }
    }

    public Cursor getMomentsByTags(Set<String> tags, boolean locationRequired) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        StringBuilder selectionBuilder = null;
        String[] selectionArgs = tags.toArray(new String[]{});
        if (selectionArgs.length > 0) {
            selectionBuilder = new StringBuilder(
                    locationRequired
                        ? MOMENT_LOCATION_LONGITUDE + " is not null and " + MOMENT_LOCATION_LATITUDE + " is not null and "
                        : "");
            for (int i = 0; i < selectionArgs.length - 1; i++) {
                selectionBuilder.append(TAGS_TABLE + "." + TAG_NAME + " = ? OR ");
            }
            selectionBuilder.append(TAGS_TABLE + "." + TAG_NAME + " = ?");
        }
        String selection = selectionBuilder == null ? null : selectionBuilder.toString();
        return database.query(MOMENTS_TABLE + " inner join " + TAGS_TABLE + " on "
                        + MOMENTS_TABLE + "." + MOMENT_ID + " = "
                        + TAGS_TABLE + "." + TAG_MOMENT_ID,
                MOMENT_COLUMNS, selection, selectionArgs, MOMENT_ID,
                "count(" + TAGS_TABLE + "." + TAG_NAME + ") > 0",
                "count(" + TAGS_TABLE + "." + TAG_NAME + ") desc");
    }

    public void deleteMomentById(UUID momentId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(MOMENTS_TABLE, MOMENT_ID + "=?", new String[]{momentId.toString()});
    }

    public void insertMoment(Moment moment) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues momentContentValues = new ContentValues();
            momentContentValues.put(MOMENT_ID, moment.getId().toString());
            momentContentValues.put(MOMENT_TITLE, moment.getTitle());
            if (moment.getDescription() != null) {
                momentContentValues.put(MOMENT_DESCRIPTION, moment.getDescription());
            }
            momentContentValues.put(MOMENT_CAPTURING_TIME, moment.getCapturingTime().getTimeInMillis());
            if (moment.getLocation() != null) {
                momentContentValues.put(MOMENT_LOCATION_LONGITUDE, moment.getLocation().getLongitude());
                momentContentValues.put(MOMENT_LOCATION_LATITUDE, moment.getLocation().getLatitude());
            }
            if (moment.getAddress() != null) {
                momentContentValues.put(MOMENT_ADDRESS, moment.getAddress());
            }
            database.insertOrThrow(MOMENTS_TABLE, null, momentContentValues);
            if (moment.getTags() != null) {
                for (String tag : moment.getTags()) {
                    ContentValues tagContentValues = new ContentValues();
                    tagContentValues.put(TAG_MOMENT_ID, moment.getId().toString());
                    tagContentValues.put(TAG_NAME, tag);
                    database.insertOrThrow(TAGS_TABLE, null, tagContentValues);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void updateMoment(Moment moment) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues momentContentValues = new ContentValues();
            momentContentValues.put(MOMENT_TITLE, moment.getTitle());
            if (moment.getDescription() != null) {
                momentContentValues.put(MOMENT_DESCRIPTION, moment.getDescription());
            }
            momentContentValues.put(MOMENT_CAPTURING_TIME, moment.getCapturingTime().getTimeInMillis());
            if (moment.getLocation() != null) {
                momentContentValues.put(MOMENT_LOCATION_LONGITUDE, moment.getLocation().getLongitude());
                momentContentValues.put(MOMENT_LOCATION_LATITUDE, moment.getLocation().getLatitude());
            }
            if (moment.getAddress() != null) {
                momentContentValues.put(MOMENT_ADDRESS, moment.getAddress());
            }
            database.update(MOMENTS_TABLE,
                    momentContentValues, MOMENT_ID + "=?", new String[]{moment.getId().toString()});
            database.delete(TAGS_TABLE, TAG_MOMENT_ID + "=?", new String[]{moment.getId().toString()});
            if (moment.getTags() != null) {
                for (String tag : moment.getTags()) {
                    ContentValues tagContentValues = new ContentValues();
                    tagContentValues.put(TAG_MOMENT_ID, moment.getId().toString());
                    tagContentValues.put(TAG_NAME, tag);
                    database.insertOrThrow(TAGS_TABLE, null, tagContentValues);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void insertMediaContent(UUID momentId, DriveId driveId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MEDIA_MOMENT_ID, momentId.toString());
        contentValues.put(MEDIA_DRIVE_ID, driveId.encodeToString());
        database.insert(MEDIA_TABLE, null, contentValues);
    }

    public void deleteMediaContent(UUID momentId, DriveId driveId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(MEDIA_TABLE, MEDIA_DRIVE_ID + "=? and " + MEDIA_MOMENT_ID + "=?",
                        new String[]{driveId.encodeToString(), momentId.toString()});
    }

    public List<DriveId> getMediaContentListByMomentId(UUID momentId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor driveIdsCursor = database.query(MEDIA_TABLE, new String[]{MEDIA_DRIVE_ID}, MEDIA_MOMENT_ID + "=?",
                new String[]{momentId.toString()}, null, null, null);
        List<DriveId> driveIdList = new ArrayList<>();
        while (driveIdsCursor.moveToNext()) {
            driveIdList.add(DriveId.decodeFromString(
                    driveIdsCursor.getString(driveIdsCursor.getColumnIndexOrThrow(MomentManager.MEDIA_DRIVE_ID))));
        }
        return driveIdList;
    }

    protected Set<String> getTagsByMomentId(UUID momentId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor tagsCursor = database.query(TAGS_TABLE, new String[]{TAG_NAME}, TAG_MOMENT_ID + "=?",
                new String[]{momentId.toString()}, null, null, null);
        Set<String> momentTags = new HashSet<>();
        while (tagsCursor.moveToNext()) {
            momentTags.add(tagsCursor.getString(tagsCursor.getColumnIndexOrThrow(MomentManager.TAG_NAME)));
        }
        return momentTags;
    }

    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_MOMENTS_CREATE);
            db.execSQL(DB_TAGS_CREATE);
            db.execSQL(DB_MEDIA_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }
    }
}
