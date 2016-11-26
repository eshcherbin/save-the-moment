package ru.spbau.savethemoment.momentmanager;

import android.location.Location;

import java.util.Calendar;
import java.util.List;

public class Moment {
    private String id;
    private String title;
    private String description;
    private Calendar capturingTime;
    private Location location;
    private List<String> tags;

    public Moment(String id, String title, String description, Calendar capturingTime,
                  Location location, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.capturingTime = capturingTime;
        this.location = location;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getCapturingTime() {
        return capturingTime;
    }

    public Location getLocation() {
        return location;
    }

    public List<String> getTags() {
        return tags;
    }
}
