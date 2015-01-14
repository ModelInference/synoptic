package synopticgwt.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;

import org.junit.Test;

/**
 * Tests the server-side configuration object.
 */
public class AppConfigurationTestWithDB {
    public static String dbPath = "." + File.separator + "test-output"
            + File.separator + "AppConfigurationTests.derby";

    @Test
    public void testGetInstanceWithDB() throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // First, set the derby db dir location.
        System.setProperty("derbyDBDir", dbPath);
        AppConfiguration conf = AppConfiguration.getInstance();
        // NOTE: conf.analyticsTrackerID is allowed to be null, so we don't test
        // it here.
        assertTrue(conf.modelExportsDir != null);
        assertTrue(conf.modelExportsURLprefix != null);
        assertTrue(conf.uploadedLogFilesDir != null);
        assertTrue(conf.synopticGWTChangesetID != null);
        assertTrue(conf.synopticChangesetID != null);

        // Derby db should not be null -- a new DB should have been created.
        assertTrue(conf.derbyDB != null);

    }
}
