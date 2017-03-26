package synoptic.tests.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.Assert;

import synoptic.main.AbstractMain;
import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.tests.SynopticTest;
import synoptic.util.Pair;

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
        String tracesBasePath = File.separator + "traces" + File.separator;
        // Two paths that we will test to find traces/args files.
        List<String> possibleTracesPaths = Arrays.asList(new String[] {
                "." + tracesBasePath, ".." + tracesBasePath });

        Collection<Object[]> argsList = new LinkedList<Object[]>();

        // List of input sub-dirs that contains end-to-end test examples.
        List<Pair<String, String>> testPaths = new ArrayList<Pair<String, String>>();
        testPaths.add(new Pair<String, String>("abstract" + File.separator,
                "mid_branching"));
        testPaths.add(new Pair<String, String>("abstract" + File.separator,
                "osx-login-example"));
        testPaths.add(new Pair<String, String>("abstract" + File.separator,
                "shopping-cart-example"));
        testPaths.add(new Pair<String, String>("abstract" + File.separator,
                "ticket-reservation-example"));

        // Examples for test generation.
        List<Pair<String, String>> testGenerationPaths = new ArrayList<Pair<String, String>>();
        testGenerationPaths.add(new Pair<String, String>("abstract"
                + File.separator, "turnstile-example"));
        testGenerationPaths.add(new Pair<String, String>("", "VerifyPin"));

        // Determine where the input traces/args are located -- try two options:
        String tracesPath = findWorkingPath(possibleTracesPaths,
                testPaths.get(0).getLeft() + testPaths.get(0).getRight()
                        + File.separator + "args.txt");

        // Compose a set of args to Synoptic for each end-to-end test case.
        for (Pair<String, String> tPath : testPaths) {
            composeArgs(tracesPath, tPath, argsList, false);
            // Check that the specific input files for this test exists.
            String argsFilename = tracesPath + tPath.getLeft()
                    + tPath.getRight() + File.separator + "args.txt";
            String traceFilename = tracesPath + tPath.getLeft()
                    + tPath.getRight() + File.separator + "trace.txt";
            File f1 = new File(argsFilename);
            File f2 = new File(traceFilename);
            if (!f1.exists() || !f2.exists()) {
                Assert.fail("Unable to find trace/argument inputs for EndtoEndMainTest with tracesPath="
                        + tracesPath);
                return argsList;
            }
        }

        // Add tests for stateful synoptic
        for (Pair<String, String> tPath : testGenerationPaths) {
            composeArgs(tracesPath, tPath, argsList, true);
        }

        return argsList;
    }

    private static void composeArgs(String tracesPath,
            Pair<String, String> tPath, Collection<Object[]> argsList,
            boolean enableTestGen) {
        // Check that the specific input files for this test exists.
        String argsFilename = tracesPath + tPath.getLeft() + tPath.getRight()
                + File.separator + "args.txt";
        String traceFilename = tracesPath + tPath.getLeft() + tPath.getRight()
                + File.separator + "trace.txt";
        File f1 = new File(argsFilename);
        File f2 = new File(traceFilename);
        if (!f1.exists() || !f2.exists()) {
            Assert.fail("Unable to find trace/argument inputs for EndtoEndMainTest: args["
                    + argsFilename + "], trace[" + traceFilename + "]");
        }

        String outputPrefix = testOutputDir + tPath.getRight();

        // TODO: A hidden path dependency is in the output file.
        // Synoptic always produces graph output, and writes it to a default
        // location, which needs to be set correctly.

        Object[] testCase;
        if (enableTestGen) {
            testCase = new Object[] { new String[] { "-o", outputPrefix, "-c",
                    argsFilename, traceFilename, "-i", "-t" } };
        } else {
            testCase = new Object[] { new String[] { "-o", outputPrefix, "-c",
                    argsFilename, traceFilename } };

        }
        argsList.add(testCase);
    }

    public EndToEndMainTests(String... argsParam) {
        this.args = argsParam;
    }

    @Test
    public void mainTest() throws Exception {
        AbstractMain.instance = null;
        SynopticMain.main(args);
    }

}
