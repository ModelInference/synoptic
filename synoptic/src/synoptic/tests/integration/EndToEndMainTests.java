package synoptic.tests.integration;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.main.Main;
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

    @Parameters
    public static Collection<Object[]> data() {
        String tracesPath = "../traces/";

        String[] testPaths = { "abstract/mid_branching/",
                "abstract/osx-login-example/",
                "abstract/shopping-cart-example/",
                "abstract/ticket-reservation-example/" };

        Collection<Object[]> argsList = new LinkedList<Object[]>();
        for (String tPath : testPaths) {
            // TODO: The -o option overrides any -o option in the args.txt file
            // and is set to empty string. This is because we need to give
            // Synoptic a correct path to the dot/png files output directory.
            // This needs to be fixed, so that we have a truly end-to-end test
            Object[] testCase = { new String[] { "-o", "", "-c",
                    tracesPath + tPath + "args.txt",
                    tracesPath + tPath + "trace.txt" } };
            argsList.add(testCase);
        }

        return argsList;
    }

    public EndToEndMainTests(String... argsParam) {
        this.args = argsParam;
    }

    @Test
    public void argsTest() throws Exception {
        Main.main(args);
    }

}
