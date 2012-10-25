package synoptic.tests.integration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.Assert;

import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.tests.SynopticTest;

/**
 * Tests main with a variety of known input log files from the traces/
 * directory. The test oracle is successful execution -- no crashes.
 * 
 * <pre>
 * TODO: Extend the oracle to expected output, which can be
 *       pre-generated for all the tested log files.
 * </pre>
 */
@RunWith(value = Parameterized.class)
public class EndToEndMainTests extends SynopticTest {
    String[] args;

    @Override
    public void setUp() throws ParseException {
        // Do not call to super, so that a new Synoptic Main is not allocated.
        return;
    }

    @Parameters
    public static Collection<Object[]> data() {
        // Where we will find traces/args files.
        String tracesPath;
        // Two paths that we will test to find traces/args files.
        String tracesPath1 = "." + File.separator + "traces" + File.separator
                + "EndToEndTests" + File.separator;
        String tracesPath2 = ".." + File.separator + "traces" + File.separator
                + "EndToEndTests" + File.separator;
        Collection<Object[]> argsList = new LinkedList<Object[]>();

        // List of input sub-dirs that contains end-to-end test examples.
        String[] testPaths = { "mid_branching", "osx-login-example",
                "shopping-cart-example", "ticket-reservation-example" };

        // Determine where the input traces/args are located -- try two options:
        // tracesPath1 and tracesPath2. Test using testPaths[0].
        File f = new File(tracesPath1 + testPaths[0] + File.separator
                + "args.txt");
        if (f.exists()) {
            tracesPath = tracesPath1;
        } else {
            f = new File(tracesPath2 + testPaths[0] + File.separator
                    + "args.txt");
            if (f.exists()) {
                tracesPath = tracesPath2;
            } else {
                Assert.fail("Unable to find trace/argument inputs for EndtoEndMainTest");
                return argsList;
            }
        }

        // Compose a set of args to Synoptic for each end-to-end test case.
        for (String tPath : testPaths) {
            // Check that the specific input files for this test exists.
            String argsFilename = tracesPath + tPath + File.separator
                    + "args.txt";
            String traceFilename = tracesPath + tPath + File.separator
                    + "trace.txt";
            File f1 = new File(argsFilename);
            File f2 = new File(traceFilename);
            if (!f1.exists() || !f2.exists()) {
                Assert.fail("Unable to find trace/argument inputs for EndtoEndMainTest");
                return argsList;
            }

            // TODO: A hidden path dependency is in the output file.
            // Synoptic always produces graph output, and writes it to a default
            // location, which needs to be set correctly.

            Object[] testCase = { new String[] { "-o", "test", "-c",
                    argsFilename, traceFilename } };
            argsList.add(testCase);
        }

        return argsList;
    }

    public EndToEndMainTests(String... argsParam) {
        this.args = argsParam;
    }

    @Test
    public void mainTest() throws Exception {
        SynopticMain.instance = null;
        SynopticMain.main(args);
    }

}
