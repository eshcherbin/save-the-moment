package ru.spbau.savethemoment.common;

import android.location.Location;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class Moment implements Serializable {
    private String id;
    private String title;
    private String description;
    private Calendar capturingTime;
    private Location location;
    private String address;
    private Set<String> tags;

    public Moment(String id, String title, String description, Calendar capturingTime,
                  Location location, String address, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.capturingTime = capturingTime;
        this.location = location;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public Set<String> getTags() {
        return tags;
    }
}
