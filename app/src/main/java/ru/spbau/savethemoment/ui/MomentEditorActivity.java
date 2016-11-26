package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;

public class MomentEditorActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Moment moment;
    private EditText title;
    private EditText description;
    private TextView date;
    private TextView time;
    private TextView location;
    private Button editDate;
    private Button editTime;
    private Button editLocation;
    private Context context;
    private boolean startedWithMoment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momenteditor);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.tool_bar_momenteditor);
        setSupportActionBar(toolbar);

        startedWithMoment = getIntent().hasExtra("Moment");
        if (startedWithMoment) {
            moment = (Moment) getIntent().getSerializableExtra("Moment");
        } else {
            moment = Moment.getCurrentMoment();
        }

        initTitle();
        initDescription();
        initDateAndTime();
        initLocation();
        //TODO: edit media content
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_momenteditor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_momenteditor_save) {
            saveTextChanges();
            if (startedWithMoment) {
                //TODO: call MomentManager to save changes
                Intent intent = new Intent();
                intent.putExtra("Moment", moment);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                //TODO: call MomentManager to save new moment
                Intent intent = new Intent(this, MomentViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Moment", moment);
                startActivity(intent);
                finish();
            }
            return true;
        }
        return false;
    }

    private void initTitle() {
        title = (EditText) findViewById(R.id.edittext_momenteditor_title);
        title.setText(moment.getTitle());
    }

    private void initDescription() {
        description = (EditText) findViewById(R.id.edittext_momenteditor_description);
        description.setText(moment.getDescription());
    }

    private void initDateAndTime() {
        date = (TextView) findViewById(R.id.textview_momenteditor_capturingtime_date);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy");
        date.setText(dateFormat.format(moment.getCapturingTime().getTime()));

        time = (TextView) findViewById(R.id.textview_momenteditor_capturingtime_time);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
        time.setText(timeFormat.format(moment.getCapturingTime().getTime()));

        editDate = (Button) findViewById(R.id.button_momenteditor_editdate);
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int curYear = calendar.get(Calendar.YEAR);
                int curMonth = calendar.get(Calendar.MONTH);
                int curDay = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                dateChanged(year, month, day, dateFormat);
                            }
                        }, curYear, curMonth, curDay);
                datePickerDialog.show();
            }
        });

        editTime = (Button) findViewById(R.id.button_momenteditor_edittime);
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int curHour = calendar.get(Calendar.HOUR_OF_DAY);
                int curMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                timeChanged(hour, minute, timeFormat);
                            }
                        }, curHour, curMinute, false);
                timePickerDialog.show();
            }
        });
    }

    private void initLocation() {
        location = (TextView) findViewById(R.id.textview_momenteditor_location);
        location.setText(moment.getAddress());

        editLocation = (Button) findViewById(R.id.button_momenteditor_editlocation);
        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle location changes
            }
        });
    }

    private void saveTextChanges() {
        moment.setTitle(title.getText().toString());
        moment.setDescription(description.getText().toString());
    }

    private void dateChanged(int year, int month, int day, SimpleDateFormat dateFormat) {
        moment.setCapturingDate(year, month, day);
        date.setText(dateFormat.format(moment.getCapturingTime().getTime()));
    }

    private void timeChanged(int hour, int minute, SimpleDateFormat timeFormat) {
        moment.setCapturingTime(hour, minute);
        time.setText(timeFormat.format(moment.getCapturingTime().getTime()));
    }
}
