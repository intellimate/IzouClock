package org.intellimate.izou.addon.izouclock;

import org.intellimate.izou.addon.izouclock.subclasses.AlarmOutput;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.resource.ResourceModel;
import org.intellimate.izou.sdk.Context;
import org.intellimate.izou.sdk.contentgenerator.ContentGenerator;
import org.intellimate.izou.sdk.contentgenerator.EventListener;
import org.intellimate.izou.sdk.frameworks.music.events.StartMusicRequest;
import org.intellimate.izou.sdk.resource.Resource;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Content Generator for IzouClock.
 */
public class ClockContentGenerator extends ContentGenerator {
    /**
     * The ID of the ClockContentGenerator
     */
    public static final String ID = ClockContentGenerator.class.getCanonicalName();

    /**
     * The ID of the resource the ClockContentGenerator creates
     */
    public static final String RESOURCE_ID = ClockContentGenerator.class.getCanonicalName()+"resource";

    /**
     * The default player associated with the clock
     */
    private final String DEFAULT_PLAYER = "jundl77.izou.izousound.outputplugin.AudioFilePlayer";

    /**
     * Creates an instance of ContentGenerator
     */
    public ClockContentGenerator(Context context) {
        super(ID, context);
    }

    @Override
    public List<? extends Resource> triggered(List<? extends ResourceModel> list, Optional<EventModel> optional) {
        // Get the audio file player that should be used
         Properties properties = getContext().getPropertiesAssistant().getProperties();
        String audioPlayerID = properties.getProperty("audioPlayerID");

        AlarmOutput alarm = new AlarmOutput(getContext(), audioPlayerID.equals(DEFAULT_PLAYER));
        Optional<Identification> identification = IdentificationManager.getInstance().getIdentification(this);

        if (identification.isPresent()) {
            IdentificationManager.getInstance()
                    .getIdentification(audioPlayerID)
                    .flatMap(target -> StartMusicRequest.createStartMusicRequest(identification.get(), target,
                            alarm.getRingtone()))
                    .ifPresent(event -> getContext().getEvents().distributor().fireEventConcurrently(event));
        }

        // Doesnt matter what is returned, because TTS data is not generated here, so object will do
        return optionalToList(createResource(RESOURCE_ID, new Object()));
    }

    @Override
    public List<? extends EventListener> getTriggeredEvents() {
        Optional<EventListener> eventListener = EventListener.createEventListener(ClockActivator.CLOCK_EVENT_ID,
                "This event triggers the alarm in the ClockAddon. It triggers a ringtone with a speach output saying" +
                        "what time it is.",
                "jundl77_izou_izouclock",
                this);
        return optionalToList(eventListener);
    }

    @Override
    public List<? extends Resource> getTriggeredResources() {
        return null;
    }
}
