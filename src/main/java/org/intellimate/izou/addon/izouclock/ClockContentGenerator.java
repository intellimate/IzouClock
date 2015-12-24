package org.intellimate.izou.addon.izouclock;

import org.intellimate.izou.addon.izouclock.subclasses.AlarmOutput;
import org.intellimate.izou.events.EventLifeCycle;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.resource.ResourceModel;
import org.intellimate.izou.sdk.Context;
import org.intellimate.izou.sdk.contentgenerator.ContentGenerator;
import org.intellimate.izou.sdk.contentgenerator.EventListener;
import org.intellimate.izou.sdk.frameworks.music.events.StartMusicRequest;
import org.intellimate.izou.sdk.frameworks.music.events.StopMusic;
import org.intellimate.izou.sdk.frameworks.presence.consumer.PresenceEventUser;
import org.intellimate.izou.sdk.resource.Resource;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Content Generator for IzouClock.
 */
public class ClockContentGenerator extends ContentGenerator implements PresenceEventUser {
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
    private final String DEFAULT_PLAYER = "org.intellimate.izou.addon.izousound.outputplugin.AudioFilePlayer";

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

        EventModel<?> event;
        if (optional.isPresent()) {
            event = optional.get();
            event.lifecycleCallback(EventLifeCycle.ENDED);
        }

        // Doesnt matter what is returned, because TTS data is not generated here, so object will do
        return optionalToList(createResource(RESOURCE_ID, new Object()));
    }

    @Override
    public List<? extends EventListener> getTriggeredEvents() {
        Optional<EventListener> eventListener = EventListener.createEventListener(ClockActivator.CLOCK_EVENT_ID,
                "This event triggers the alarm in the ClockAddon. It triggers a ringtone with a speach output saying" +
                        "what time it is.",
                "intellimate_izou_addon_izouclock",
                this);
        return optionalToList(eventListener);
    }

    @Override
    public List<? extends Resource> getTriggeredResources() {
        return null;
    }

    /**
     * This method can either fire alarms until the user is present or stop firing alarms unless the user is present
     */
    public void actOnPresence(Optional<Identification> source, AlarmOutput alarm, String audioPlayerID) {
        Properties properties = getContext().getPropertiesAssistant().getProperties();
        final boolean fireUntilPresent = Boolean.parseBoolean(properties.getProperty("fireUntilPresent"));
        final boolean fireWhilePresent = Boolean.parseBoolean(properties.getProperty("fireOnPresent"));

        getContext().getThreadPool().getThreadPool().submit(() -> {
            if (fireUntilPresent) {
                AtomicBoolean present = new AtomicBoolean(false);
                nextPresence(false, false).thenAccept(presenceEvent -> {
                    if (source.isPresent()) {
                        present.set(true);
                        IdentificationManager.getInstance()
                                .getIdentification(audioPlayerID)
                                .flatMap(target -> StopMusic.createStopMusic(source.get(), target))
                                .ifPresent(event -> getContext().getEvents().distributor().fireEventConcurrently(event));
                    }
                });
                while (!present.get()) {
                    IdentificationManager.getInstance()
                            .getIdentification(audioPlayerID)
                            .flatMap(target -> StartMusicRequest.createStartMusicRequest(source.get(), target,
                                    alarm.getRingtone()))
                            .ifPresent(event -> getContext().getEvents().distributor().fireEventConcurrently(event));

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        error("Unable to sleep", e);
                    }
                }
            } else if (fireWhilePresent) {
                AtomicBoolean present = new AtomicBoolean(false);
                nextLeaving(false).thenAccept(presenceEvent -> {
                    if (source.isPresent()) {
                        IdentificationManager.getInstance()
                                .getIdentification(audioPlayerID)
                                .flatMap(target -> StartMusicRequest.createStartMusicRequest(source.get(), target,
                                        alarm.getRingtone()))
                                .ifPresent(event -> getContext().getEvents().distributor().fireEventConcurrently(event));
                    }
                });
                nextPresence(false, false).thenAccept(presenceEvent -> {
                    if (source.isPresent()) {
                        IdentificationManager.getInstance()
                                .getIdentification(audioPlayerID)
                                .flatMap(target -> StartMusicRequest.createStartMusicRequest(source.get(), target,
                                        alarm.getRingtone()))
                                .ifPresent(event -> getContext().getEvents().distributor().fireEventConcurrently(event));
                    }
                });
                while (present.get()) {
                    present.set(true);
                    IdentificationManager.getInstance()
                            .getIdentification(audioPlayerID)
                            .flatMap(target -> StopMusic.createStopMusic(source.get(), target))
                            .ifPresent(event -> getContext().getEvents().distributor().fireEventConcurrently(event));

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        error("Unable to sleep", e);
                    }
                }
            }
        });
    }
}
