package synopticgwt.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;

import org.junit.Test;

/**
 * Tests the server-side configuration object.
 */
public class AppConfigurationTests {
    public static String dbPath = "." + File.separator + "test-output"
            + File.separator + "AppConfigurationTests.derby";

    @Test
    public void testGetInstanceNoDB() throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        AppConfiguration conf = AppConfiguration.getInstance(null);
        // NOTE: conf.analyticsTrackerID is allowed to be null.
        assertTrue(conf.modelExportsDir != null);
        assertTrue(conf.modelExportsURLprefix != null);
        assertTrue(conf.uploadedLogFilesDir != null);
        assertTrue(conf.synopticGWTChangesetID != null);
        assertTrue(conf.synopticChangesetID != null);
        // derbyDB is null because System property with derby db dir is not set.
        assertTrue(conf.derbyDB == null);
    }

    @Test
    public void testGetInstanceWithDB() throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // First, set the derby db dir location.
        System.setProperty("derbyDBDir", dbPath);
        AppConfiguration conf = AppConfiguration.getInstance(null);
        // NOTE: conf.analyticsTrackerID is allowed to be null.
        assertTrue(conf.modelExportsDir != null);
        assertTrue(conf.modelExportsURLprefix != null);
        assertTrue(conf.uploadedLogFilesDir != null);
        assertTrue(conf.synopticGWTChangesetID != null);
        assertTrue(conf.synopticChangesetID != null);

        // Derby db should not be null -- a new DB should have been created.
        assertTrue(conf.derbyDB != null);

    }
}
