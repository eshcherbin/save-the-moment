package ru.spbau.savethemoment.momentmanager;

import android.location.Location;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MomentManager {
    public static List<Moment> getMoments() {
        return loadMoments();
    }

    public static List<Moment> getMomentsByTags(List<String> tags) {
        List<Moment> allMoments = loadMoments();
        List<Moment> filteredMoments = new LinkedList<>();
        for (Moment moment : allMoments) {
            if (moment.getTags().containsAll(tags)) {
                filteredMoments.add(moment);
            }
        }
        return filteredMoments;
    }

    private static List<Moment> loadMoments() {
        //TODO: implement loading moments from Google Drive
        return Collections.singletonList(new Moment("id", "Test moment", "This is a test moment",
                                                    Calendar.getInstance(), new Location(""),
                                                    Collections.singletonList("testTag")));
    }
}