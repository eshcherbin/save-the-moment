package ru.spbau.savethemoment.momentmanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class MomentManager extends ContentProvider {

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

    private static final String TAG_ID = "_ID";
    private static final String TAG_MOMENT_ID = "moment_id";
    private static final String TAG_NAME = "name";

    private static final String DB_MOMENTS_CREATE = "create table " + MOMENTS_TABLE + "("
            + MOMENT_ID + " integer primary key autoincrement, "
            + MOMENT_TITLE + "text, "
            + MOMENT_DESCRIPTION + "text, "
            + MOMENT_CAPTURING_TIME + "integer, "
            + MOMENT_LOCATION_LONGITUDE + "double, "
            + MOMENT_LOCATION_LATITUDE + "double" + ");";
    private static final String DB_TAGS_CREATE = "create table " + TAGS_TABLE + "("
            + TAG_ID + " integer primary key autoincrement, "
            + TAG_MOMENT_ID + " integer, "
            + TAG_NAME + "text\n"
            + "foreign key (" + TAG_MOMENT_ID + ") references "
            + MOMENTS_TABLE + "(" + MOMENT_ID + ")" + ");";

    private static final String AUTHORITY = "ru.spbau.savethemoment.momentmanager";

    private static final String MOMENT_PATH = "moments";
    private static final String TAG_PATH = "tags";

    private static final String MOMENT_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY
            + "." + MOMENT_PATH;
    private static final String MOMENT_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + MOMENT_PATH;
    private static final String TAG_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY
            + "." + TAG_PATH;
    private static final String TAG_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + TAG_PATH;

    private static final int URI_MOMENTS = 1;
    private static final int URI_MOMENTS_ID = 2;
    private static final int URI_TAGS = 3;
    private static final int URI_TAGS_ID = 4;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MOMENT_PATH, URI_MOMENTS);
        uriMatcher.addURI(AUTHORITY, MOMENT_PATH + "/#", URI_MOMENTS_ID);
        uriMatcher.addURI(AUTHORITY, TAG_PATH, URI_TAGS);
        uriMatcher.addURI(AUTHORITY, TAG_PATH + "/#", URI_TAGS_ID);
    }

    private DBHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    public static final Uri MOMENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY +
            "/" + MOMENT_PATH);
    public static final Uri TAGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY +
            "/" + TAG_PATH);

    public MomentManager() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
