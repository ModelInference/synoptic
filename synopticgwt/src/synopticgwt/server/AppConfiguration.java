package synopticgwt.server;

/**
 * Singleton class that takes care of loading in all of the application
 * configuration parameters. Currently, these are passed to the application as
 * system properties.
 */
public class AppConfiguration {
    private static final AppConfiguration instance = new AppConfiguration();

    /**
     * The Google Analytics tracking identifier.
     */
    public final String analyticsTrackerID;

    /**
     * The directory to which files are currently exported to.
     */
    public final String userExport;

    /**
     * Private constructor prevents instantiation from other classes
     */
    private AppConfiguration() {
        analyticsTrackerID = System.getProperty("analyticsTrackerID");
        userExport = System.getProperty("userExport", "userexport/");
    }

    public static AppConfiguration getInstance() {
        return instance;
    }
}
