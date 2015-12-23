package org.intellimate.izou.addon.izouclock;

import ro.fortsoft.pf4j.PluginWrapper;

/**
 * Default zip file manager needed to load classes correctly
 */
public class ZipFileManager extends org.intellimate.izou.sdk.addon.ZipFileManager {
    public ZipFileManager(PluginWrapper wrapper) {
        super(wrapper);
    }
}
