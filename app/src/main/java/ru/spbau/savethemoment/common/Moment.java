package ru.spbau.savethemoment.common;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Moment implements Parcelable {
    private UUID id;
    private String title;
    private String description;
    private Calendar capturingTime;
    private Location location;
    private String address;
    private Set<String> tags;

    public Moment(UUID id, String title, String description, Calendar capturingTime,
                  Location location, String address, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.capturingTime = capturingTime;
        this.location = location;
        this.address = address;
        this.tags = tags;
    }

    protected Moment(Parcel in) {
        id = (UUID) in.readSerializable();
        title = in.readString();
        description = in.readString();
        capturingTime = (Calendar) in.readSerializable();
        location = in.readParcelable(Location.class.getClassLoader());
        address = in.readString();
        tags = (Set<String>) in.readSerializable();
    }

    public UUID getId() {
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCapturingDate(int year, int month, int day) {
        this.capturingTime.set(Calendar.YEAR, year);
        this.capturingTime.set(Calendar.MONTH, month);
        this.capturingTime.set(Calendar.DAY_OF_MONTH, day);
    }

    public void setCapturingTime(int hour, int minute) {
        this.capturingTime.set(Calendar.HOUR_OF_DAY, hour);
        this.capturingTime.set(Calendar.MINUTE, minute);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void deleteTag(String tag) {
        tags.remove(tag);
    }

    public static Moment createCurrentMoment() {
        return new Moment(UUID.randomUUID(), "", "", Calendar.getInstance(), null, null,
                new HashSet<String>());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeSerializable(capturingTime);
        dest.writeParcelable(location, flags);
        dest.writeString(address);
        dest.writeSerializable((Serializable) tags);
    }

    public static final Creator<Moment> CREATOR = new Creator<Moment>() {
        @Override
        public Moment createFromParcel(Parcel in) {
            return new Moment(in);
        }

        @Override
        public Moment[] newArray(int size) {
            return new Moment[size];
        }
    };
}
