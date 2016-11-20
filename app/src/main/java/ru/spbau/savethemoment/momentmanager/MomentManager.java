package ru.spbau.savethemoment.momentmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Set;

public class MomentManager {

    private static final String DB_NAME = "moments_db";
    private static final int DB_VERSION = 1;

    private static final String MOMENTS_TABLE = "moments";
    private static final String TAGS_TABLE = "tags";

    private static final String MOMENT_ID = "_ID";
    private static final String MOMENT_TITLE = "title";
    private static final String MOMENT_DESCRIPTION = "description";
    private static final String MOMENT_CAPTURING_TIME = "capturing_time";
    private static final String MOMENT_LOCATION_LONGITUDE = "location_longitude";
    private static final String MOMENT_LOCATION_LATITUDE = "location_latitude";

    private static final String TAG_MOMENT_ID = "moment_id";
    private static final String TAG_NAME = "name";

    private static final String[] MOMENT_COLUMNS = new String[]{MOMENT_ID, MOMENT_TITLE,
            MOMENT_DESCRIPTION, MOMENT_CAPTURING_TIME,
            MOMENT_LOCATION_LONGITUDE, MOMENT_LOCATION_LATITUDE};

    private static final String DB_MOMENTS_CREATE = "create table " + MOMENTS_TABLE + "("
            + MOMENT_ID + " integer primary key autoincrement, "
            + MOMENT_TITLE + "text, "
            + MOMENT_DESCRIPTION + "text, "
            + MOMENT_CAPTURING_TIME + "integer, "
            + MOMENT_LOCATION_LONGITUDE + "double, "
            + MOMENT_LOCATION_LATITUDE + "double" + ");";
    private static final String DB_TAGS_CREATE = "create table " + TAGS_TABLE + "("
            + TAG_MOMENT_ID + " integer, "
            + TAG_NAME + "text\n"
            + "foreign key (" + TAG_MOMENT_ID + ") references "
            + MOMENTS_TABLE + "(" + MOMENT_ID + ")" + ");";

    private DBHelper dbHelper;

    public MomentManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public Cursor getMoments() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        return database.query(MOMENTS_TABLE, MOMENT_COLUMNS, null, null, null, null, null);
    }

    public Cursor getTagsByMomentId(int momentId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        return database.query(TAGS_TABLE, new String[]{TAG_NAME}, TAG_MOMENT_ID + "=?",
                new String[]{String.valueOf(momentId)}, null, null, null);
    }

    public Cursor getMomentsByTags(Set<String> tags) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        //TODO: implement querying by tags as in issue #5
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void deleteMomentById(int momentId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TAGS_TABLE, TAG_MOMENT_ID + "=?", new String[]{String.valueOf(momentId)});
        database.delete(MOMENTS_TABLE, MOMENT_ID + "=?", new String[]{String.valueOf(momentId)});
    }

    public void insertMoment(Moment moment) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        //TODO: implement inserting a moment and all its tags
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
    }
}
