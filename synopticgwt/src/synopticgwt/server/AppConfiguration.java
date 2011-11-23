package synopticgwt.server;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

/**
 * Singleton class that takes care of loading in all of the application
 * configuration parameters. Currently, these are passed to the application as
 * system properties.
 */
public class AppConfiguration {
    private static AppConfiguration instance;

    /**
     * The Google Analytics tracking identifier.
     */
    public final String analyticsTrackerID;

    /**
     * The directory to which model dot/png files are exported.
     */
    public final String modelExportsDir;

    /**
     * URL prefix to use to allow clients to access exported model files. This
     * must map to modelExportsDir (defined above) on the local filesystem.
     */
    public final String modelExportsURLprefix;

    /**
     * The directory to which submitted logs are saved.
     */
    public final String uploadedLogFilesDir;

    /**
     * Mercurial changeset id embedded in MANIFEST.MF
     */
    public String changesetID;

    /**
     * Private constructor prevents instantiation from other classes
     * 
     * @throws IOException
     */
    private AppConfiguration(ServletContext context) {
        analyticsTrackerID = System.getProperty("analyticsTrackerID", null);

        String modelExportsDir_ = System.getProperty("modelExportsDir", null);
        if (modelExportsDir_ == null) {
            modelExportsDir = "model-exports/";
        } else {
            // Relative path within the war archive.
            modelExportsDir = modelExportsDir_ + "/";
        }

        modelExportsURLprefix = System.getProperty("modelExportsURLprefix",
                "model-exports") + "/";

        String uploadedLogFilesDir_ = System.getProperty("logFilesDir", null);
        if (uploadedLogFilesDir_ == null) {
            // Relative path within the war archive.
            uploadedLogFilesDir = "uploaded-logfiles/";
        } else {
            uploadedLogFilesDir = uploadedLogFilesDir_ + "/";
        }

        try {
            // Extract the hg changeset id from MANIFEST.MF
            Properties prop = new Properties();
            prop.load(context.getResourceAsStream("/META-INF/MANIFEST.MF"));
            // System.out.println("All attributes:" +
            // prop.stringPropertyNames());
            // System.out.println(prop.getProperty("ChangesetID"));
            changesetID = prop.getProperty("ChangesetID");
        } catch (Exception e) {
            changesetID = "unknown";
        }
    }

    public static AppConfiguration getInstance(ServletContext context) {
        if (instance != null) {
            return instance;
        }
        return new AppConfiguration(context);
    }
}
