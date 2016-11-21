package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;

public class MomentViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_view);

        Moment moment = (Moment) getIntent().getSerializableExtra("Moment");

        TextView title = (TextView) findViewById(R.id.textview_momentview_title);
        title.setText(moment.getTitle());

        TextView description = (TextView) findViewById(R.id.textview_momentview_description);
        description.setText(moment.getDescription());

        TextView location = (TextView) findViewById(R.id.textview_momentview_location);
        if (moment.getAddress() == null) {
            if (moment.getLocation() == null) {
                location.setText("Location is unknown");
            } else {
                location.setText("Show on map");
            }
        } else {
            location.setText(moment.getAddress());
        }
        if (moment.getLocation() != null) {
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: on click display the moment on map
                }
            });
        }

        TextView capturingTime = (TextView) findViewById(R.id.textview_momentview_capturingtime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss");
        capturingTime.setText(dateFormat.format(moment.getCapturingTime().getTime()));

        //TODO: displaying media content
    }

}