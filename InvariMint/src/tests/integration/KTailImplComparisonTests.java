package tests.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import main.InvariMintMain;
import main.InvariMintOptions;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import tests.InvariMintTest;

public class KTailImplComparisonTests extends InvariMintTest {

    /**
     * Tests InvariMint KTails versus Synoptic KTails.
     */
    @Test
    public void compareFinalModels() throws Exception {

        Map<String, String[]> configs = new HashMap<String, String[]>();
        // Parsing args:
        configs.put("simple-model", new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>" });
        configs.put("osx-login-example", new String[] { "-r", "(?<TYPE>.+)",
                "-s", "--" });

        int k;
        for (k = 1; k < 5; k++) {
            logger.info("Comparing kTails and InvMint-kTails with k = " + k);

            for (Entry<String, String[]> testCase : configs.entrySet()) {
                String dir = testCase.getKey();
                String[] parsingArgs = testCase.getValue();

                String tPath = ".." + File.separator + "traces"
                        + File.separator;
                String path = tPath + "abstract" + File.separator + dir
                        + File.separator;

                String[] args = (String[]) ArrayUtils.addAll(parsingArgs,
                        new String[] { // "--exportStdAlgPGraph",
                                // "--exportStdAlgDFA",
                                "--compareToStandardAlg",
                                "--minimizeIntersections", "--logLvlVerbose",
                                "--invMintKTails=true", "--kTailLength",
                                k + "", "-o", testOutputDir + dir,
                                path + "trace.txt" });

                InvariMintOptions opts = new InvariMintOptions(args);
                InvariMintMain main = new InvariMintMain(opts);
                main.runInvariMint();

                // Assert that the invarimint model and the standard algorithm
                // models are identical.
                assertTrue("Failure on " + dir + " when k = " + k,
                        main.isEqualToStandardAlg());

            }
        }
    }
}
