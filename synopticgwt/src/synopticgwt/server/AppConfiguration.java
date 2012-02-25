package synopticgwt.server;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import synoptic.main.Main;

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
     * Whether or not user voice should be enabled on the site.
     */
    public final Boolean userVoiceEnabled;

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
     * Hg changeset id embedded in MANIFEST.MF corresponding to the SynopticGWT
     * project.
     */
    public String synopticGWTChangesetID;

    /**
     * Hg changeset id for the Synoptic jar that is used by the SynopticGWT
     * project.
     */
    public String synopticChangesetID;

    /**
     * Private constructor prevents instantiation from other classes
     * 
     * @throws IOException
     */
    private AppConfiguration(ServletContext context) {
        analyticsTrackerID = System.getProperty("analyticsTrackerID", null);

        if (System.getProperty("userVoiceEnabled", null) != null) {
            userVoiceEnabled = true;
        } else {
            userVoiceEnabled = false;
        }

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
            // Extract the hg changeset id from war archive MANIFEST.MF
            Properties prop = new Properties();
            prop.load(context.getResourceAsStream("/META-INF/MANIFEST.MF"));
            synopticGWTChangesetID = prop.getProperty("ChangesetID");
        } catch (Exception e) {
            synopticGWTChangesetID = "unknown";
        }
        if (synopticGWTChangesetID == null) {
            synopticGWTChangesetID = "unknown";
        }

        synopticChangesetID = Main.getHgChangesetID();
        if (synopticChangesetID == null) {
            synopticChangesetID = "unknown";
        }
    }

    public static AppConfiguration getInstance(ServletContext context) {
        if (instance != null) {
            return instance;
        }
        return new AppConfiguration(context);
    }
}
