package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;
import ru.spbau.savethemoment.momentmanager.MomentManager;

public class MomentEditorActivity extends AppCompatActivity {
    private static final int CHOOSE_LOCATION_REQUEST_CODE = 0;

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

    private MomentManager momentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momenteditor);
        context = this;

        momentManager = new MomentManager(this);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_momenteditor);
        setSupportActionBar(toolbar);

        startedWithMoment = getIntent().hasExtra("Moment");
        if (startedWithMoment) {
            moment = getIntent().getParcelableExtra("Moment");
        } else {
            moment = Moment.createCurrentMoment();
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
            if (checkIfTitleEmpty()) {
                Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show();
                return true;
            }
            saveTextChanges();
            if (startedWithMoment) {
                momentManager.updateMoment(moment);
                Intent intent = new Intent();
                intent.putExtra("Moment", moment);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                momentManager.insertMoment(moment);
                Intent intent = new Intent(this, MomentViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("MomentId", moment.getId());
                startActivity(intent);
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                LatLng latLng = data.getParcelableExtra(ChooseLocationActivity.POSITION_LAT_LNG_NAME);
                Location momentLocation = new Location("");
                momentLocation.setLatitude(latLng.latitude);
                momentLocation.setLongitude(latLng.longitude);
                moment.setLocation(momentLocation);
                String address = data.getStringExtra(ChooseLocationActivity.ADDRESS);
                location.setText(address);
                moment.setAddress(address);
            }
        }
    }

    boolean checkIfTitleEmpty() {
        return title.getText().toString().length() <= 0;
    }

    void setErrorOnTitle() {
        if (checkIfTitleEmpty()) {
            title.setError("Title is required");
        } else {
            title.setError(null);
        }
    }

    private void initTitle() {
        title = (EditText) findViewById(R.id.edittext_momenteditor_title);
        title.setText(moment.getTitle());
        setErrorOnTitle();
        title.addTextChangedListener(new TextWatcher()  {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s)  {
                setErrorOnTitle();
            }
        });
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
                Intent intent = new Intent(context, ChooseLocationActivity.class);
                Location currentMomentLocation = moment.getLocation();
                if (currentMomentLocation != null) {
                    intent.putExtra(ChooseLocationActivity.POSITION_LAT_LNG_NAME,
                            new LatLng(currentMomentLocation.getLatitude(),
                                    currentMomentLocation.getLongitude()));
                }
                startActivityForResult(intent, CHOOSE_LOCATION_REQUEST_CODE);
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
