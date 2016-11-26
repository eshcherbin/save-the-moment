package ru.spbau.savethemoment.ui;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;


@RunWith(AndroidJUnit4.class)
public class MomentEditorActivityTest {

    @Rule
    public ActivityTestRule<MomentEditorActivity> activityRule = new ActivityTestRule<>(
            MomentEditorActivity.class,
            true,
            false);

    @Test
    public void onCreateFromMain() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("Parent", "Main");
        activityRule.launchActivity(intent);

        Moment moment = Moment.getCurrentMoment();

        Espresso.onView(ViewMatchers.withId(R.id.edittext_momenteditor_title))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(moment.getTitle())));
        Espresso.onView(ViewMatchers.withId(R.id.edittext_momenteditor_description))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(moment.getDescription())));
    }

    @Test
    public void onCreateFromMomentView() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Moment moment = new Moment(UUID.randomUUID(), "MyTitle", "MyDescription", calendar, null, "MyAddress", null);
        Intent intent = new Intent();
        intent.putExtra("Parent", "MomentView");
        intent.putExtra("Moment", moment);

        activityRule.launchActivity(intent);

        Espresso.onView(ViewMatchers.withId(R.id.edittext_momenteditor_title))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(moment.getTitle())));
        Espresso.onView(ViewMatchers.withId(R.id.edittext_momenteditor_description))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(moment.getDescription())));

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");

        Espresso.onView(ViewMatchers.withId(R.id.textview_momenteditor_capturingtime_date))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(dateFormat.format(calendar.getTime()))));

        Espresso.onView(ViewMatchers.withId(R.id.textview_momenteditor_capturingtime_time))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(timeFormat.format(calendar.getTime()))));

        Espresso.onView(ViewMatchers.withId(R.id.textview_momenteditor_location))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(moment.getAddress())));
    }

}