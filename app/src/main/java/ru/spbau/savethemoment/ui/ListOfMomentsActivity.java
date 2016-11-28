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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.momentmanager.MomentManager;

public class ListOfMomentsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    private static final String PATTERN_ONE_TAG = "[а-яА-Яa-zA-Z0-9#\\-]+";
    private static final String PATTERN_TAG_SEPARATOR = ",";

    private Toolbar toolbar;
    private ListView listViewMoments;
    private ListOfMomentsAdapter listOfMomentsAdapter;
    private Pattern oneTagPattern;
    private Pattern tagsSeparatorPattern;

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
        listViewMoments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String stringMomentId = cursor.getString(cursor.getColumnIndexOrThrow(MomentManager.MOMENT_ID));
                UUID momentId =
                        UUID.fromString(stringMomentId);
                Intent intent = new Intent(ListOfMomentsActivity.this, MomentViewActivity.class);
                intent.putExtra("MomentId", momentId);
                startActivity(intent);
            }
        });

        oneTagPattern = Pattern.compile(PATTERN_ONE_TAG);
        tagsSeparatorPattern = Pattern.compile(PATTERN_TAG_SEPARATOR);

        final EditText editTextTagsToFilter = (EditText) findViewById(R.id.edittext_list_of_moments_tags);
        editTextTagsToFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.edittext_list_of_moments_tags && !hasFocus) {
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        editTextTagsToFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    if (before > 0) {
                        getLoaderManager().restartLoader(LOADER_ID, null, ListOfMomentsActivity.this);
                    }
                    return;
                }
                String[] tags = tagsSeparatorPattern.split(s, -1);
                for (String tag : tags) {
                    if (!oneTagPattern.matcher(tag).matches()) {
                        return;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("Tags", new HashSet<>(Arrays.asList(tags)));
                getLoaderManager().restartLoader(LOADER_ID, bundle, ListOfMomentsActivity.this);
            }

            @Override
            public void afterTextChanged(Editable s) {

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
    protected void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (args != null && args.keySet().contains("Tags")) {
            return new MomentsLoader(this, (Set<String>) args.getSerializable("Tags"));
        } else {
            return new MomentsLoader(this, null);
        }
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
}

