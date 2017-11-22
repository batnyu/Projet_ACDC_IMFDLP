package acdc.Services;

/**
 * <b>Settings is a singleton (Initialization-on-demand holder idiom) that register the settings</b>
 *
 * <p>
 * Useful to get the settings from anywhere.
 *
 * @author Baptiste
 * @version 1.0
 */
public class Settings {

    private Settings() {
    }

    private static class Holder {
        private static final Settings INSTANCE = new Settings();
    }

    public static Settings getInstance() {
        return Holder.INSTANCE;
    }

    //Default destination value
    private String pathCacheHash = "cacheHash.txt";

    //ACCESSORS
    public String getPathCacheHash() {
        return pathCacheHash;
    }

    public void setPathCacheHash(String pathCacheHash) {
        this.pathCacheHash = pathCacheHash;
    }
}
