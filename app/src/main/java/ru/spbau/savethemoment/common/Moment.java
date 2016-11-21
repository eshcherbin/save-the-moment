package ru.spbau.savethemoment.common;

import android.location.Location;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public class Moment implements Serializable {
    private String id;
    private String title;
    private String description;
    private String address;
    private Calendar capturingTime;
    private Location location;
    private List<String> tags;

    public Moment(String id, String title, String description, String address,
                  Calendar capturingTime, Location location, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.address = address;
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

    public String getAddress() {
        return address;
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
