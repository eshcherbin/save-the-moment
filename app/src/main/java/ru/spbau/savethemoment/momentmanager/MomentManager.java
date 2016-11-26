package ru.spbau.savethemoment.momentmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Set;

import ru.spbau.savethemoment.common.Moment;

public class MomentManager {

    private static final String DB_NAME = "moments_db";
    private static final int DB_VERSION = 1;

    private static final String MOMENTS_TABLE = "moments";
    private static final String TAGS_TABLE = "tags";

    public static final String MOMENT_ID = "_id";
    public static final String MOMENT_TITLE = "title";
    public static final String MOMENT_DESCRIPTION = "description";
    public static final String MOMENT_CAPTURING_TIME = "capturing_time";
    public static final String MOMENT_LOCATION_LONGITUDE = "location_longitude";
    public static final String MOMENT_LOCATION_LATITUDE = "location_latitude";
    public static final String MOMENT_ADDRESS = "address";

    public static final String TAG_MOMENT_ID = "moment_id";
    public static final String TAG_NAME = "name";

    private static final String[] MOMENT_COLUMNS = new String[]{MOMENT_ID, MOMENT_TITLE,
            MOMENT_DESCRIPTION, MOMENT_CAPTURING_TIME,
            MOMENT_LOCATION_LONGITUDE, MOMENT_LOCATION_LATITUDE, MOMENT_ADDRESS};

    private static final String DB_MOMENTS_CREATE = "create table " + MOMENTS_TABLE + "("
            + MOMENT_ID + " text primary key not null, "
            + MOMENT_TITLE + " text, "
            + MOMENT_DESCRIPTION + " text, "
            + MOMENT_CAPTURING_TIME + " integer, "
            + MOMENT_LOCATION_LONGITUDE + " double, "
            + MOMENT_LOCATION_LATITUDE + " double, "
            + MOMENT_ADDRESS + " text " + ");";
    private static final String DB_TAGS_CREATE = "create table " + TAGS_TABLE + "("
            + TAG_MOMENT_ID + " integer not null, "
            + TAG_NAME + " text, "
            + "foreign key(" + TAG_MOMENT_ID + ") references " + MOMENTS_TABLE + "(" + MOMENT_ID + ") on delete cascade"
            + ");";

    private DBHelper dbHelper;

    public MomentManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public Cursor getMoments() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        return database.query(MOMENTS_TABLE, MOMENT_COLUMNS, null, null, null, null, null);
    }

    public Cursor getMomentById(String momentId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        return database.query(MOMENTS_TABLE, MOMENT_COLUMNS, MOMENT_ID + "=?",
                new String[]{momentId}, null, null, null);
    }

    public Cursor getTagsByMomentId(String momentId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        return database.query(TAGS_TABLE, new String[]{TAG_NAME}, TAG_MOMENT_ID + "=?",
                new String[]{momentId}, null, null, null);
    }

    public Cursor getMomentsByTags(Set<String> tags) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        StringBuilder selectionBuilder = null;
        String[] selectionArgs = tags.toArray(new String[]{});
        if (selectionArgs.length > 0) {
            selectionBuilder = new StringBuilder();
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
                "count(" + TAGS_TABLE + "." + TAG_NAME + ") = "
                        + String.valueOf(tags.size()), null);
    }

    public void deleteMomentById(String momentId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(MOMENTS_TABLE, MOMENT_ID + "=?", new String[]{momentId});
    }

    public void insertMoment(Moment moment) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues momentContentValues = new ContentValues();
            momentContentValues.put(MOMENT_ID, moment.getId());
            momentContentValues.put(MOMENT_TITLE, moment.getTitle());
            momentContentValues.put(MOMENT_DESCRIPTION, moment.getDescription());
            momentContentValues.put(MOMENT_CAPTURING_TIME, moment.getCapturingTime().getTimeInMillis());
            momentContentValues.put(MOMENT_LOCATION_LONGITUDE, moment.getLocation().getLongitude());
            momentContentValues.put(MOMENT_LOCATION_LATITUDE, moment.getLocation().getLatitude());
            momentContentValues.put(MOMENT_ADDRESS, moment.getAddress());
            database.insertOrThrow(MOMENTS_TABLE, null, momentContentValues);
            for (String tag : moment.getTags()) {
                ContentValues tagContentValues = new ContentValues();
                tagContentValues.put(TAG_MOMENT_ID, moment.getId());
                tagContentValues.put(TAG_NAME, tag);
                database.insertOrThrow(TAGS_TABLE, null, tagContentValues);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_MOMENTS_CREATE);
            db.execSQL(DB_TAGS_CREATE);
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
