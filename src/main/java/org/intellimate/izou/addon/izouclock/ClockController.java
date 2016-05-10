package org.intellimate.izou.addon.izouclock;

import org.intellimate.izou.addon.izouclock.subclasses.AlarmActivator;
import org.intellimate.izou.sdk.Context;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * <p>
 *     The ClockController is an interface that can be used to schedule alarms programmatically.
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
public class ClockController {
    private static ClockController clockController;

    private Context context;
    private ClockActivator clockActivator;

    /**
     * <p>
     *     Get the single instance of the ClockController.
     * </p>
     *
     * @return The single instance of the ClockController.
     */
    public static ClockController getInstance() {
        return clockController;
    }

    /**
     * <p>
     *     Create a new ClockController instance.
     * </p>
     *
     * @param context The context of the addOn.
     * @param clockActivator The {@link ClockActivator} used to get access to currently loaded alarms.
     */
    ClockController(Context context, ClockActivator clockActivator) {
        this.context = context;
        this.clockActivator = clockActivator;
        clockController = this;
    }

    /**
     * <p>
     *     Schedule an alarm that will set off a set of events. If everything was successful the alarm number is returned.
     *     This number can be used to delete the alarm again with the unschedule method. It is an integer ranging from 1
     *     to 999.
     * </p>
     * <p>
     *     If the alarm was not scheduled correctly, -1 will be returned.
     * </p>
     *
     * @param day The day the alarm should go off. The day has to be a valid day of the week (monday - sunday).
     * @param repeat True if the alarm should repeat, else false. If set to false, the alarm only executes once.
     * @param hour The hour of the day that the alarm should go off. An integer between 0 and 24.
     * @param minute The minute of the hour the alarm should go off. An integer between 0 and 60.
     * @param second The second of the minute the alarm should go off. An integer between 0 and 60.
     * @param events The set of events the alarm should fire when it executes.
     * @return The alarm number (an integer between 1 and 999), or -1 if the alarm failed to be scheduled.
     */
    public synchronized int scheduleAlarm(String day, boolean repeat, int hour, int minute, int second, List<String> events) {
        // Make sure day is a valid day of the week
        if (!day.equals("monday") && !day.equals("tuesday") && !day.equals("wednesday") && !day.equals("thursday")
                && !day.equals("friday") && !day.equals("saturday") && !day.equals("sunday")) {
            context.getLogger().error("Unable to schedule alarm: " + day + " is not a valid day of the week");
            return -1;
        }

        // Make sure hour is in between 0 and 24
        if (hour < 0 || hour > 24) {
            context.getLogger().error("Unable to schedule alarm: " + hour + " is not a valid hour of the day");
            return -1;
        }

        // Make sure minute is in between 0 and 60
        if (minute < 0 || minute > 60) {
            context.getLogger().error("Unable to schedule alarm: " + minute + " is not a valid minute of a hour");
            return -1;
        }

        // Make sure second is in between 0 and 60
        if (second < 0 || second > 60) {
            context.getLogger().error("Unable to schedule alarm: " + second + " is not a second of a minute");
            return -1;
        }

        // Generate the alarm number
        int alarmNumber = -1;
        while (alarmNumber == -1 || clockActivator.getSchedualedAlarms().contains(AlarmActivator.ALARM_PREFIX + alarmNumber)) {
            alarmNumber = (int) (Math.random() * 998) + 1;
        }

        // Generate the full alarm settings string
        String alarmSettings = AlarmActivator.ALARM_PREFIX + alarmNumber + " = " + day + ";" + repeat + ";" + hour + ";"
                + minute + ";" + second + ";" + events.stream().reduce("", (eventString, event) -> eventString += event + ";");
        alarmSettings = alarmSettings.substring(0, alarmSettings.length() - 1);

        // Write alarm settings string to file
        try {
            File propertiesFile = context.getPropertiesAssistant().getPropertiesFile();
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(propertiesFile));

            // Find the line number of where to insert the alarm
            int position = 0;
            lineNumberReader.setLineNumber(position);
            while (!lineNumberReader.readLine().contains("# ClockController Alarms")) {
                position++;
            }

            // Add the string to the properties file so that the alarm can be triggered there
            List<String> lines = Files.readAllLines(propertiesFile.toPath(), StandardCharsets.UTF_8);
            lines.add(position + 1, alarmSettings);
            Files.write(propertiesFile.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            context.getLogger().error("Unable to write alarm settings string " + AlarmActivator.ALARM_PREFIX
                    + alarmNumber + " to file");
        }

        return alarmNumber;
    }

    /**
     * <p>
     *     Deletes the alarm with the given alarm number from izou.
     * </p>
     *
     * @param alarmNumber The alarm number of the alarm to delete.
     * @return True if the alarm was removed successfully and otherwise false.
     */
    public synchronized boolean unscheduleAlarm(int alarmNumber) {
        String alarmName = AlarmActivator.ALARM_PREFIX + alarmNumber;

        try {
            File propertiesFile = context.getPropertiesAssistant().getPropertiesFile();
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(propertiesFile));

            // Find the line number of the alarm to remove
            int position = 0;
            lineNumberReader.setLineNumber(position);
            while (!lineNumberReader.readLine().contains(alarmName)) {
                position++;
            }

            // Remove the alarm
            List<String> lines = Files.readAllLines(propertiesFile.toPath(), StandardCharsets.UTF_8);
            lines.remove(position);
            Files.write(propertiesFile.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            context.getLogger().error("Unable to remove alarm settings string " + alarmName + " from file");
            return false;
        }

        return true;
    }
}
