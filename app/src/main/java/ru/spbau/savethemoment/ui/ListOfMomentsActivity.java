package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import ru.spbau.savethemoment.R;

public class ListOfMomentsActivity extends Activity {

    public void onFilterButtonClicked(View view) {
        //TODO: implement filtering
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_moments);

        ListView listViewMoments = (ListView) findViewById(R.id.listview_list_of_moments);
        //TODO: populate this listView
    }
}
