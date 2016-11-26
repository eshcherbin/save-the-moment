package ru.spbau.savethemoment.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;

public class MomentViewActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momentview);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_momentview);
        setSupportActionBar(toolbar);


        Moment moment = (Moment) getIntent().getSerializableExtra("Moment");

        TextView title = (TextView) findViewById(R.id.textview_momentview_title);
        title.setText(moment.getTitle());

        TextView description = (TextView) findViewById(R.id.textview_momentview_description);
        description.setText(moment.getDescription());

        TextView location = (TextView) findViewById(R.id.textview_momentview_location);
        if (moment.getAddress() != null) {
            location.setText(moment.getAddress());
        } else {
            location.setText(null);
        }

        TextView capturingTime = (TextView) findViewById(R.id.textview_momentview_capturingtime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss");
        capturingTime.setText(dateFormat.format(moment.getCapturingTime().getTime()));

        //TODO: displaying media content
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_momentview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_momentview_onmap) {
            //TODO: show moment on map
            return true;
        }
        return false;
    }


}