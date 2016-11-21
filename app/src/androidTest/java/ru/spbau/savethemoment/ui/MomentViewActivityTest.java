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

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;

@RunWith(AndroidJUnit4.class)
public class MomentViewActivityTest {

    @Rule
    public ActivityTestRule<MomentViewActivity> activityRule = new ActivityTestRule<>(
            MomentViewActivity.class,
            true,
            false); // launch activity

    @Test
    public void onCreate() throws Exception {
        Intent intent = new Intent();
        Calendar calendar = Calendar.getInstance();
        Moment moment = new Moment("Id", "MyTitle", "MyDescription", null, calendar, null, null);
        intent.putExtra("Moment", moment);
        activityRule.launchActivity(intent);

        Espresso.onView(ViewMatchers.withId(R.id.textview_momentview_title))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText("MyTitle")));
        Espresso.onView(ViewMatchers.withId(R.id.textview_momentview_description))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText("MyDescription")));

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss");
        Espresso.onView(ViewMatchers.withId(R.id.textview_momentview_capturingtime))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText(dateFormat.format(calendar.getTime()))));

        Espresso.onView(ViewMatchers.withId(R.id.textview_momentview_location))
                .check(ViewAssertions
                        .matches(ViewMatchers.withText("Location is unknown"))); 
    }

}