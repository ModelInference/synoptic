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
     * The directory to which model dot/png files are exported.
     */
    public final String modelExportsDir;

    /**
     * The directory to which submitted logs are saved.
     */
    public final String uploadedLogFilesDir;

    /**
     * Private constructor prevents instantiation from other classes
     */
    private AppConfiguration() {
        analyticsTrackerID = System.getProperty("analyticsTrackerID");
        modelExportsDir = System.getProperty("modelExportsDir",
                "model-exports/");
        uploadedLogFilesDir = System.getProperty("logFilesDir",
                "uploaded-logfiles/");
    }

    public static AppConfiguration getInstance() {
        return instance;
    }
}
