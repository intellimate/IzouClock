package org.intellimate.izou.addon.izouclock.subclasses;

import org.intellimate.izou.sdk.Context;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AlarmActivator is an alarm that is used to compare the scheduled time with the actual time to know when to fire its
 * event.
 */
public class AlarmActivator extends Alarm {
    /**
     * A time buffer in which the alarm can still be triggered, in seconds
     */
    public static final int TRIGGER_BUFFER = 5;
    private HashMap<String, AlarmRepeatSettings> settingsMap;
    public static final String ALARM_PREFIX = "alarmSettings";

    /**
     * Creates a new AlarmActivator
     *
     * @param context the context of the addOn
     */
    public AlarmActivator(Context context) {
        super(context);
        generateRepeats();
    }

    /**
     * Checks the activity of the alarm by comparing the current time to the scheduled alarm time. If they match, an
     * array with the key of the alarm and its repeat state is returned. A trigger buffer is applied to the current
     * seconds in order to give the alarm a broader time range of activation.
     *
     * @param day the current day
     * @param hours the current hour
     * @param minutes the current minute
     * @param seconds the current second
     *
     * @return An array consisting of 2 elements:  The name of the alarm to activate, if it should be activated
     * (else null), and the repeat state of the alarm
     */
    public String[] checkActivity(int day, int hours, int minutes, int seconds) {
        String[] activityState = new String[2];

        for (Map.Entry<String, AlarmRepeatSettings> entry : settingsMap.entrySet()) {
            AlarmRepeatSettings repeatSettings = entry.getValue();
            if (repeatSettings.getDay() == day
                    && repeatSettings.getHours() == hours
                    && repeatSettings.getMinutes() == minutes
                    && ((repeatSettings.getSeconds() - TRIGGER_BUFFER <= seconds)
                    && (repeatSettings.getSeconds() + TRIGGER_BUFFER >= seconds))) {
                activityState[0] = entry.getKey();
                activityState[1] = Boolean.toString(repeatSettings.isState());
                return activityState;
            }
        }
        activityState[0] = "null";
        activityState[1] = "false";
        return activityState;
    }

    private void generateRepeats() {
        this.settingsMap = new HashMap<>();
        List<String> alarmRepeats = getAlarmRepeats();
        for (String alarm : alarmRepeats) {
            AlarmRepeatSettings repeatSettings = new AlarmRepeatSettings(alarm, getContext());
            this.settingsMap.put(alarm, repeatSettings);
        }
    }

    private List<String> getAlarmRepeats() {
        return getContext().getPropertiesAssistant().getProperties().stringPropertyNames().stream()
                .filter(prop -> prop.contains(ALARM_PREFIX))
                .collect(Collectors.toList());
    }

    /**
     * Gets the settings map of the alarm. The settings map contains all the information about when the alarm should be
     * activated and whether it should repeat or not.
     *
     * @return the settings map of the alarm.
     */
    public HashMap<String, AlarmRepeatSettings> getSettingsMap() {
        return settingsMap;
    }
}

