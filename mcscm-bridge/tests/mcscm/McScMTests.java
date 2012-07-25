package mcscm;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

public class McScMTests {

    McScM bridge;
    String verifyPath;
    String scmFilePrefix;

    protected static Logger logger;

    @Before
    public void setUp() {
        // Determine whether to use the Linux or the OSX McScM binary.
        String osStr = null;
        if (Os.isLinux()) {
            osStr = "linux";
        } else if (Os.isMac()) {
            osStr = "osx";
        } else {
            fail("Running on an unsupported OS (not Linux, and not Mac).");
        }

        // NOTE: We assume the tests are run from synoptic/mcscm-bridge/
        verifyPath = "../bin/mcscm.verify." + osStr;
        scmFilePrefix = "./tests/mcscm/";

        logger = Logger.getLogger("TestMcScM");
        logger.setLevel(Level.INFO);
        bridge = new McScM(verifyPath);
    }

    /**
     * Reads a text file from the current directory and returns it as a single
     * string.
     * 
     * @throws IOException
     */
    public String readScmFile(String filename) throws IOException {
        String filePath = scmFilePrefix + filename;
        BufferedReader in = new BufferedReader(new FileReader(filePath));

        StringBuilder everything = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            everything.append(line);
            everything.append("\n");
        }
        return everything.toString();
    }

    /**
     * Empty scm input should result in a syntax error.
     * 
     * @throws IOException
     */
    @Test(expected = ScmSyntaxException.class)
    public void testEmptyScmInput() throws IOException {
        try {
            bridge.verify("");
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        bridge.getVerifyResult();
        fail("getVerifyResult should have thrown an exception.");
    }

    /**
     * Test verify on a safe model.
     * 
     * @throws IOException
     */
    @Test
    public void testSafeScmInput() throws IOException {
        String scmStr = readScmFile("ABP_safe.scm");

        try {
            bridge.verify(scmStr);
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        VerifyResult result = bridge.getVerifyResult();
        assert (result.modelIsSafe());
    }

    /**
     * Test verify on an unsafe model.
     * 
     * @throws IOException
     */
    @Test
    public void testUnsafeScmInput() throws IOException {
        String scmStr = readScmFile("ABP_unsafe.scm");

        try {
            bridge.verify(scmStr);
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        VerifyResult result = bridge.getVerifyResult();
        assert (!result.modelIsSafe());
    }
}
