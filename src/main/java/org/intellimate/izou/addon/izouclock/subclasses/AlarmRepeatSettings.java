package org.intellimate.izou.addon.izouclock.subclasses;

import org.intellimate.izou.sdk.Context;

import java.util.*;

/**
 * AlarmRepeatSettings contains all the data about when the alarm should be activated, and whether it should be
 * repeated.
 */
@SuppressWarnings("UnusedDeclaration")
public class AlarmRepeatSettings {
    @SuppressWarnings("FieldCanBeLocal")
    private Context context;
    private int day;
    private boolean state;
    private int hours;
    private int minutes;
    private int seconds;
    private Set<String> eventsToFire;

    /**
     * Creates a new AlarmRepeatSettings object
     *
     * @param key the key of the alarm to get the settings for
     */
    public AlarmRepeatSettings(String key, Context context) {
        this.context = context;
        eventsToFire = new HashSet<>();
        getSettings(context.getPropertiesAssistant().getProperty(key));
    }

    /**
     * Gets the settings for a specific alarm
     *
     * @param settings the value associated to the alarm (key) from the properties file
     */
    public void getSettings(String settings) {
        // Settings should be in the form of day;state;hour;minute;second
        String[] parts = settings.split(";");
        int numberOfParts = parts.length;

        if (numberOfParts > 0) {
            parts[0] = parts[0].toLowerCase();
            try {
                day = checkDay(parts[0]);
            } catch (IllegalStateException e) {
                context.getLogger().warn("Day has to be a valid day", e);
            }
        }
        if (numberOfParts > 1)
            state = analyseDayState(parts[1]);
        if (numberOfParts > 2)
            hours = analyseTime(parts[2]);
        if (numberOfParts > 3)
            minutes = analyseTime(parts[3]);
        if (numberOfParts > 4)
            seconds = analyseTime(parts[4]);
        if (numberOfParts > 5) {
            for (int i = 5; i < parts.length; i++) {
                eventsToFire.add(checkEventID(parts[i]));
            }
        } else {
            if (!eventsToFire.contains("izou.alarm")) {
                eventsToFire.add("izou.alarm");
            }
        }

        int index = 6;
        boolean eventsExist = true;
        while (eventsExist) {
            if (numberOfParts > index) {
                String eventID = checkEventID(parts[index]);
                if (!eventsToFire.contains(eventID)) {
                    eventsToFire.add(eventID);
                }
                index++;
            } else {
                eventsExist = false;
            }
        }

    }

    private int checkDay(String value) throws IllegalStateException {
        switch (value) {
            case "sunday":
                return 1;
            case "monday":
                return 2;
            case "tuesday":
                return 3;
            case "wednesday":
                return 4;
            case "thursday":
                return 5;
            case "friday":
                return 6;
            case "saturday":
                return 7;
        }
        throw new IllegalStateException("day has to be an existing day (i.e. monday-sunday)");
    }

    private int analyseTime(String value) throws IllegalStateException {
        return Integer.parseInt(value);
    }

    private boolean analyseDayState(String value) throws IllegalStateException {
        switch (value) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new IllegalStateException("alarm day's have to be set to true or false");
        }
    }

    private String checkEventID(String eventID) {
        if (context.getPropertiesAssistant().getEventPropertiesAssistant().getEventID(eventID) == null) {
            return "izou.alarm";
        } else {
            return context.getPropertiesAssistant().getEventPropertiesAssistant().getEventID(eventID);
        }
    }

    /**
     * Gets the day of the settings
     *
     * @return the day of the settings
     */
    public int getDay() {
        return day;
    }

    /**
     * Sets the day of the settings
     *
     * @param day the day to set
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * Gets the repeat state of the settings
     *
     * @return the repeat state of the settings
     */
    public boolean isState() {
        return state;
    }

    /**
     * Sets the repeat state of the settings
     *
     * @param state the state of the settings to set
     */
    public void setState(boolean state) {
        this.state = state;
    }

    /**
     * Gets the hour of the settings
     *
     * @return the hour of the settings
     */
    public int getHours() {
        return hours;
    }

    /**
     * Sets the hour of the settings
     *
     * @param hours the hour of the settings to set
     */
    public void setHours(int hours) {
        this.hours = hours;
    }

    /**
     * Gets the minute of the settings
     *
     * @return the minute of the settings
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Sets the minute of the settings
     *
     * @param minutes the minute of the settings to set
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    /**
     * Gets the second of the settings
     *
     * @return the second of the settings
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Sets the second of the settings
     *
     * @param seconds the second of the settings to set
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    /**
     * Gets all events to fire
     *
     * @return all events to fire
     */
    public List<String> getEventsToFire() {
        return new ArrayList<>(eventsToFire);
    }
}
