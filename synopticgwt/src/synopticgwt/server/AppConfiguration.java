package synopticgwt.server;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;

import synoptic.main.Main;
import synopticgwt.server.db.DerbyDB;

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
     * Instance of Derby database.
     */
    public final DerbyDB derbyDB;

    /**
     * Visitor id.
     */
    public int vID;

    /**
     * Private constructor prevents instantiation from other classes
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     * @throws IOException
     */
    private AppConfiguration(ServletContext context) throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
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

        String derbyDBDir = System.getProperty("derbyDBDir", null);
        if (derbyDBDir == null) {
            // Disabled DerbyDB support.
            derbyDB = null;
        } else {
            File f = new File(derbyDBDir);
            // TODO If given empty existing directory, unable to create a
            // database, throws error. Look further into a solution for this.
            if (f.exists()) {
                // Open existing database.
                derbyDB = DerbyDB.getInstance(derbyDBDir, false);
            } else {
                // Create new database.
                derbyDB = DerbyDB.getInstance(derbyDBDir, true);
            }
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

        try {
            synopticChangesetID = Main.getHgChangesetID();
        } catch (IOException e) {
            // TODO: log exception.
            synopticChangesetID = null;
        }
        if (synopticChangesetID == null) {
            synopticChangesetID = "unknown";
        }
    }

    public static AppConfiguration getInstance(ServletContext context)
            throws SQLException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        if (instance != null) {
            return instance;
        }
        return new AppConfiguration(context);
    }

    /**
     * Closes DerbyDB cleanly if server process terminates.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            derbyDB.shutdown();
        } finally {
            super.finalize();
        }
    }
}
