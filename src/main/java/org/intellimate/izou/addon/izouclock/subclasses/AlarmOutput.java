package org.intellimate.izou.addon.izouclock.subclasses;

import org.intellimate.izou.addon.izouclock.ClockAddOn;
import org.intellimate.izou.addon.izousound.TrackInfoGenerator;
import org.intellimate.izou.sdk.Context;
import org.intellimate.izou.sdk.frameworks.music.player.TrackInfo;

import java.util.Properties;

/**
 * AlarmOutput contains the ringtone data.
 */
public class AlarmOutput extends Alarm {
    private TrackInfo ringtone;
    private boolean ringtoneState;

    /**
     * Creates a new AlarmOutput object which contains the ringtone data to be played when the alarm is activated
     *
     * @param context the context of the addOn
     */
    public AlarmOutput(Context context, boolean defaultPlayer) {
        super(context);
        this.ringtoneState = getPropertiesBoolean("ringtoneActivityState");
        generateRingtone(defaultPlayer);
    }

    private void generateRingtone(boolean defaultPlayer) {
        Properties properties = getContext().getPropertiesAssistant().getProperties();
        String audioFileName = properties.getProperty("audioFileName");
        audioFileName = ClockAddOn.ADDON_DATA_PATH_LOCAL + audioFileName;

        if (defaultPlayer) {
            ringtone = new TrackInfo(null, null, null, null, null, audioFileName);
        }

        TrackInfoGenerator trackInfoGenerator = new TrackInfoGenerator();
        int startPoint = 0;
        int duration = 0;
        int endPoint;

        if (ringtoneState) {
            try {
                startPoint = Integer.parseInt(properties.getProperty("startPointAudioFile"));
                duration =  Integer.parseInt(properties.getProperty("durationAudioFile"));
            } catch (NumberFormatException e) {
                getContext().getLogger().error("Start or end value for track info is not an integer, setting length of " +
                        "track to full length");
                startPoint = -1;
                duration = -1;
            }
        } else {
            audioFileName = null;
        }

        if (duration == -1) {
            endPoint = -1;
        } else {
            endPoint = startPoint + duration;
        }

        ringtone = trackInfoGenerator.generatFileTrackInfo(audioFileName, startPoint, endPoint);
    }

    /**
     * Gets the ringtone (of type {@link TrackInfo})
     *
     * @return the ringtone object
     */
    public TrackInfo getRingtone() {
        return ringtone;
    }

    /**
     * Sets the ringtone (of type {@link TrackInfo})
     *
     * @param ringtone the ringtone object to set
     */
    public void setRingtone(TrackInfo ringtone) {
        this.ringtone = ringtone;
    }

    /**
     * Gets the ringtone state
     *
     * @return true if the ringtone is active, else false
     */
    public boolean isRingtoneState() {
        return ringtoneState;
    }

    /**
     * Sets the ringtone state
     *
     * @param ringtoneState the new state to set for the ringtone
     */
    public void setRingtoneState(boolean ringtoneState) {
        this.ringtoneState = ringtoneState;
    }
}

