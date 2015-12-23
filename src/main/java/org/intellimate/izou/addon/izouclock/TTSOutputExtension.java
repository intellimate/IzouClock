package org.intellimate.izou.addon.izouclock;

import leanderk.izou.tts.outputextension.TTSData;
import leanderk.izou.tts.outputplugin.TTSOutputPlugin;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.resource.ResourceModel;
import org.intellimate.izou.sdk.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * TTS Output Extension for ClockAddOn
 */
public class TTSOutputExtension extends leanderk.izou.tts.outputextension.TTSOutputExtension {
    /**
     * The ID of the TTSOutputExtension
     */
    public static final String ID = TTSOutputExtension.class.getCanonicalName();

    /**
     * Creates a new OutputExtension for IzouTTS so that it can communicate with the user
     *
     * @param context the addOn's context
     */
    public TTSOutputExtension(Context context) {
        super(ID, context);
        addResourceIdToWishList(ClockContentGenerator.RESOURCE_ID);
        this.setPluginId(TTSOutputPlugin.ID);
    }

    @Override
    public TTSData generateSentence(EventModel event) {
        ResourceModel<Object> resource = null;
        try {
            //noinspection unchecked
            resource = event
                    .getListResourceContainer()
                    .provideResource(ClockContentGenerator.RESOURCE_ID)
                    .get(0);
        } catch (Exception e) {
            error("Failed to get resource for clockTTS", e);
        }

        if (resource == null) {
            warn("Not able to obtain resource");
            return null;
        }

        Calendar calendar = new GregorianCalendar();
        int hour24  = calendar.get(Calendar.HOUR_OF_DAY);
        int hour12  = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);

        String state;
        if (hour24 < 12 || hour24 == 24) {
            state = "am";
        } else {
            state = "pm";
        }

        HashMap<String, String> arguments = new HashMap<>();
        arguments.put("hour24", String.valueOf(hour24));
        arguments.put("hour12", String.valueOf(hour12));
        arguments.put("min", String.valueOf(min));
        arguments.put("state", state);

        String ttsString;
        if (hour24 < 12) {
            ttsString = getWords("wakeUpMessageMorning", arguments);
        } else {
            ttsString = getWords("wakeUpMessageDay", arguments);
        }

        TTSData ttsData = TTSData.createTTSData(ttsString, getLocale(), 0, ID);
        ttsData.setAfterID("leanderk.izou.tts.commonextensions.WelcomeExtension");

        debug("Converted ClockFetchedData resource to a TTSData object");
        return ttsData;
    }

    @Override
    public boolean canGenerateForLanguage(String locale) {
        if (locale.equals(new Locale("de").getLanguage())) {
            return true;
        } else if (locale.equals(new Locale("en").getLanguage())) {
            return true;
        }
        return false;
    }
}
