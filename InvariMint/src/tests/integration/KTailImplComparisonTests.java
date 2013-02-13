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
        configs.put("ktail-simple-model-example", new String[] {
                "-r", "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m",
                "\\k<nodename>" });
        configs.put("osx-login-example", new String[] { "-r",
                "(?<TYPE>.+)", "-s", "--" });

        for (int k = 0; k < 5; k++) {
            for (Entry<String, String[]> testCase : configs
                    .entrySet()) {
                String dir = testCase.getKey();
                String[] parsingArgs = testCase.getValue();

                String tPath = ".." + File.separator + "traces"
                        + File.separator;
                String path = tPath + "abstract" + File.separator
                        + "simple-model" + File.separator;

                String[] args = (String[]) ArrayUtils.addAll(parsingArgs,
                        new String[] { "--invMintKTails=true", "--kTailLength",
                                k + "", "-o", testOutputDir + dir,
                                path + "trace.txt" });

                assertTrue(
                        "Failure on " + dir + " when k = " + k,
                        InvariMintMain
                                .compareInvariMintSynoptic(new InvariMintOptions(
                                        args)));
            }
        }
    }
}
