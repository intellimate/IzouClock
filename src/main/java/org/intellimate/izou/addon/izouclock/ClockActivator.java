package org.intellimate.izou.addon.izouclock;

import org.intellimate.izou.addon.izouclock.subclasses.AlarmActivator;
import org.intellimate.izou.addon.izouclock.subclasses.AlarmRepeatSettings;
import org.intellimate.izou.sdk.Context;
import org.intellimate.izou.sdk.activator.Activator;
import org.intellimate.izou.sdk.events.CommonEvents;
import org.intellimate.izou.sdk.properties.PropertiesAssistant;
import org.intellimate.izou.system.file.FileSubscriber;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Activator based on a time. You can set a time when this activator should be fired in the properties file of this
 * addOn.
 */
public class ClockActivator extends Activator implements FileSubscriber {
    /**
     * The ID of the ClockActivator
     */
    public static final String ID = ClockAddOn.class.getCanonicalName();
    public static final String CLOCK_EVENT_ID = ClockAddOn.class.getCanonicalName() + "clockEvent";
    private final long SECONDS_IN_WEEK = 7 * 24 * 60 * 60;
    private LoggedScheduledExecutor executorService;
    private HashMap<String, ScheduledFuture<?>> scheduledFutureMap;
    private HashMap<String, Boolean> alarmRepeatMap;
    private AlarmActivator alarmActivator;
    private Consumer<PropertiesAssistant> propertiesAssistantConsumer = PropertiesAssistant -> update();

    /**
     * Creates a new ClockActivator object. It fires when certain time requirements are met.
     *
     * @param context The context of the addOn
     */
    public ClockActivator(Context context) {
        super(context, ID);
        executorService = new LoggedScheduledExecutor(context, 20);
        alarmRepeatMap = new HashMap<>();
        scheduledFutureMap = new HashMap<>();
    }

    /**
     * Updates the repeat map. If a repeat setting is no longer valid, it is removed.
     *
     * @param settingsMap the settings map (as a {@link java.util.HashMap}) to be updated.
     */
    public void repeatsMapUpdate(HashMap<String, AlarmRepeatSettings> settingsMap) {
        alarmRepeatMap.entrySet()
                .stream()
                .filter(repeat -> !settingsMap.containsKey(repeat.getKey()))
                .forEach(repeat -> alarmRepeatMap.remove(repeat.getKey()));
    }

    @Override
    public void activatorStarts() {
        getContext().getPropertiesAssistant().registerUpdateListener(propertiesAssistantConsumer);
        alarmActivator = new AlarmActivator(getContext());
        repeatsMapUpdate(alarmActivator.getSettingsMap());
        update();
        stop();
    }

    private void checkAndFireEvent(String alarmName, boolean repeatSetting, List<String> eventsToFire)
            throws InterruptedException {
        debug("Checking repeat permissions for " + alarmName);
        if (!alarmRepeatMap.containsKey(alarmName))
            alarmRepeatMap.put(alarmName, true);
        if (alarmRepeatMap.get(alarmName) || repeatSetting) {
            alarmRepeatMap.put(alarmName, repeatSetting);

            fire(CommonEvents.Type.RESPONSE_TYPE, eventsToFire);
        }
    }

    @Override
    public void update() {
        debug("Updating alarms");
        alarmActivator = new AlarmActivator(getContext());
        repeatsMapUpdate(alarmActivator.getSettingsMap());

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String dateTime = dateFormat.format(date);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        String[] dateParts = dateTime.split(" ");
        String[] timeParts = dateParts[1].split(":");
        int currentHours = Integer.parseInt(timeParts[0]);
        int currentMinutes = Integer.parseInt(timeParts[1]);
        int currentSeconds = Integer.parseInt(timeParts[2]);

        HashMap<String, AlarmRepeatSettings> repeatSettings = alarmActivator.getSettingsMap();

        clearFuturesMap();
        resetThreads(dayOfWeek, currentHours, currentMinutes, currentSeconds, repeatSettings);
    }

    private void resetThreads(int dayOfWeek, int currentHours, int currentMinutes, int currentSeconds, HashMap<String,
            AlarmRepeatSettings> repeatSettings) {
        for (String alarm : repeatSettings.keySet()) {
            //calculating seconds passed since sunday at 00:00:00, and then figuring out time interval from now until
            //the alarm should be activated
            AlarmRepeatSettings settings = repeatSettings.get(alarm);

            debug("Calculating current time interval from sunday at 00:00:00");

            //calculating current interval since sunday at 00:00:00
            //1 is subtracted from dayOfWeek because hours of the same day will still be added
            long currentInterval = (dayOfWeek - 1) * 24 * 60 * 60;
            currentInterval += currentHours * 60 * 60;
            currentInterval += currentMinutes * 60;
            currentInterval += currentSeconds;

            //calculating alarm interval since sunday at 00:00:00
            debug("Calculating alarm time interval from sunday at 00:00:00");
            long alarmInterval = (settings.getDay() - 1) * 24 * 60 * 60;
            alarmInterval += settings.getHours() * 60 * 60;
            alarmInterval += settings.getMinutes() * 60;
            alarmInterval += settings.getSeconds();

            //calculating real interval from now until alarm should be activated
            debug("Calculating time interval until alarm");
            long interval = calculateWaitingTime(currentInterval, alarmInterval);

            Runnable runAlarm = () -> {
                try {
                    checkAndFireEvent(alarm, settings.isState(), settings.getEventsToFire());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

            String events = "";
            for (String eventToFire : settings.getEventsToFire()) {
                events += eventToFire + ", ";
            }
            events = events.substring(0, events.length() - 2);

            debug("Scheduling " + alarm + " - alarm will trigger " + events + " in " + interval + " seconds");
            ScheduledFuture<?> alarmHandler = executorService.scheduleAtFixedRate(runAlarm, interval, SECONDS_IN_WEEK,
                    SECONDS);
            scheduledFutureMap.put(alarm, alarmHandler);
        }
    }

    private void clearFuturesMap() {
        for (ScheduledFuture future : scheduledFutureMap.values()) {
            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            while (!future.cancel(false) || (currentTime - startTime > 30000)) {
                currentTime = System.currentTimeMillis();
            }

            if (!future.isCancelled()) {
                future.cancel(true);
            }
        }
        scheduledFutureMap.clear();
    }

    private long calculateWaitingTime(long currentInterval, long alarmInterval) {
        if (alarmInterval - currentInterval >= 0) {
            return alarmInterval - currentInterval;
        } else {
            return alarmInterval - currentInterval + SECONDS_IN_WEEK;
        }
    }
}
