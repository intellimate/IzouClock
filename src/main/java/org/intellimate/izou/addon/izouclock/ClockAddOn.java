package org.intellimate.izou.addon.izouclock;

import org.intellimate.izou.output.OutputExtensionModel;
import org.intellimate.izou.sdk.activator.Activator;
import org.intellimate.izou.sdk.addon.AddOn;
import org.intellimate.izou.sdk.contentgenerator.ContentGenerator;
import org.intellimate.izou.sdk.events.EventsController;
import org.intellimate.izou.sdk.output.OutputPlugin;
import ro.fortsoft.pf4j.Extension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * The ClockAddOn is a time based activator that can be set in the properties files.
 */
@Extension
public class ClockAddOn extends AddOn {
    /**
     * The ID of the ClockAddOn
     */
    public static final String ADDON_ID = ClockAddOn.class.getCanonicalName();

    /**
     * The local data path where external data should be stored (sound files etc.)
     */
    public static String ADDON_DATA_PATH_LOCAL;

    /**
     * Creates a new ClockAddOn and registers all necessary parts.
     */
    public ClockAddOn() {
        super(ADDON_ID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepare() {
        ADDON_DATA_PATH_LOCAL = getContext().getFiles().getResourceLocation() + File.separator + "IzouClock" + File.separator;

        File newDir = new File(ADDON_DATA_PATH_LOCAL.substring(0, ADDON_DATA_PATH_LOCAL.length() - 1));

        if (!newDir.exists()) {
            try {
                newDir.mkdir();
            } catch (SecurityException e) {
                getContext().getLogger().warn("Unable to make IzouClock resource directory", e);
            }
        }

        moveRingtones();
    }

    @Override
    public Activator[] registerActivator() {
        Activator[] activators = new Activator[1];
        activators[0] = new ClockActivator(getContext());
        return activators;
    }

    @Override
    public ContentGenerator[] registerContentGenerator() {
        ContentGenerator[] contentGenerators = new ContentGenerator[1];
        contentGenerators[0] = new ClockContentGenerator(getContext());
        return contentGenerators;
    }

    @Override
    public EventsController[] registerEventController() {
        return null;
    }

    @Override
    public OutputPlugin[] registerOutputPlugin() {
        return null;
    }

    @Override
    public OutputExtensionModel[] registerOutputExtension() {
        OutputExtensionModel[] outputExtensions = new OutputExtensionModel[1];
        outputExtensions[0] = new TTSOutputExtension(getContext());
        return outputExtensions;
    }

    @Override
    public String getID() {
        return ADDON_ID;
    }

    /**
     * Moves the ringtones from the addOn into the addOn's resource folder in Izou
     */
    private void moveRingtones() {
        String path = getContext().getFiles().getLibLocation() +
                getContext().getAddOn().getPlugin().getPluginPath() + File.separator + "classes" + File.separator
                + "ringtones" +  File.separator;

        try {
            File dir = new File(path);
            if (dir.isDirectory()) {
                File[] content = dir.listFiles();

                for (File file : content) {
                    Files.move(file.toPath(), Paths.get(ADDON_DATA_PATH_LOCAL + file.getName()), StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                }
            }
        } catch (NullPointerException | IOException e) {
            getContext().getLogger().error("Unable to move file", e);
        }
    }
}
