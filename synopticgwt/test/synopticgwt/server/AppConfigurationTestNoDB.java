package synopticgwt.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;

import org.junit.Test;

/**
 * Tests the server-side configuration object.
 */
public class AppConfigurationTestNoDB {
    public static String dbPath = "." + File.separator + "test-output"
            + File.separator + "AppConfigurationTests.derby";

    @Test
    public void testGetInstanceNoDB() throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        // Hacks to reset of testing environment:
        // 1. Remove the derby db dir location property
        // 2. Reset the static singleton inside of AppConfiguration.
        System.clearProperty("derbyDBDir");
        AppConfiguration.resetInstance();

        AppConfiguration conf = AppConfiguration.getInstance();
        // NOTE: conf.analyticsTrackerID is allowed to be null, so we don't test
        // it here.
        assertTrue(conf.modelExportsDir != null);
        assertTrue(conf.modelExportsURLprefix != null);
        assertTrue(conf.uploadedLogFilesDir != null);
        assertTrue(conf.synopticGWTChangesetID != null);
        assertTrue(conf.synopticChangesetID != null);
        // derbyDB is null because System property with derby db dir is not set.
        assertTrue(conf.derbyDB == null);
    }

}
