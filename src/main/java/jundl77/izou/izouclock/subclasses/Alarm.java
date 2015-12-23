package jundl77.izou.izouclock.subclasses;

import org.intellimate.izou.sdk.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * The Alarm class serves as a general layout for an alarm, it has important data yet is not too specific, hence it is
 * abstract. {@link AlarmOutput} and {@link AlarmActivator} both extend if and have their own purposes, one to deal
 * with output and one to deal with the activating process, respectively.
 */
public abstract class Alarm { // Does not extend AddOnModule because it is repeatedly created during alarm updates,
    // which causes duplicate registration --> crash
    private Calendar calendar;
    private boolean state;
    private Context context;

    /**
     * Creates a new Alarm object through an extended constructor, as Alarm is abstract.
     *
     * @param context the context of the addOn
     */
    public Alarm(Context context) {
        this.context = context;
        this.state = getPropertiesBoolean("alarmActivityState");
        this.calendar = new GregorianCalendar();
    }

    /**
     * Gets all boolean
     *
     * @param key the of the boolean to get in the properties file
     * @return the value of that key
     */
    public boolean getPropertiesBoolean(String key) {
        String property = getContext().getPropertiesAssistant().getProperty(key);
        if (property == null) {
            context.getLogger().warn(key + " was set to null, false has been returned");
            return false;
        } else if (property.equals("true")) {
            return true;
        } else if (property.equals("false")) {
            return false;
        } else {
            context.getLogger().warn(property + " was set to null, false has been returned");
            return false;
        }
    }

    /**
     * Gets the calendar object of type {@link java.util.Calendar}
     *
     * @return the calendar object
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * Sets the calendar object of type {@link java.util.Calendar}
     *
     * @param calendar the calendar object
     */
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Gets state of alarm
     *
     * @return the state of the alarm
     */
    public boolean isState() {
        return state;
    }

    /**
     * Sets the state of the alarm
     *
     * @param state the state of the alarm
     */
    public void setState(boolean state) {
        this.state = state;
    }

    /**
     * Gets the context of the addOn
     *
     * @return the context of the addOn
     */
    protected Context getContext() {
        return context;
    }
}
