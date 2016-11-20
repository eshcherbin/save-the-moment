package ru.spbau.savethemoment.momentmanager;

import android.location.Location;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class Moment {
    private int id;
    private String title;
    private String description;
    private Calendar capturingTime;
    private Location location;
    private Set<String> tags;

    public Moment(int id, String title, String description, Calendar capturingTime,
                  Location location, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.capturingTime = capturingTime;
        this.location = location;
        this.tags = tags;
    }

    public int getId() {
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

    public Set<String> getTags() {
        return tags;
    }
}
